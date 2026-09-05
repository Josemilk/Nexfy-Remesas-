package com.example.ui

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Registra un cliente nuevo en Firebase Authentication
     * y crea su perfil correspondiente en Firestore.
     *
     * @return UID real generado por Firebase.
     */
    suspend fun registerUser(
        name: String,
        idCard: String,
        phone: String,
        email: String,
        password: String
    ): String {

        val cleanName = name.trim()
        val cleanId = idCard.trim()
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password

        // -----------------------------
        // VALIDACIONES
        // -----------------------------

        require(cleanName.isNotBlank()) {
            "El nombre es obligatorio."
        }

        require(cleanId.isNotBlank()) {
            "El número de identidad es obligatorio."
        }

        require(cleanPhone.isNotBlank()) {
            "El teléfono es obligatorio."
        }

        require(cleanEmail.isNotBlank()) {
            "El correo electrónico es obligatorio."
        }

        require(android.util.Patterns.EMAIL_ADDRESS
            .matcher(cleanEmail)
            .matches()
        ) {
            "El correo electrónico no es válido."
        }

        require(cleanPassword.length >= 6) {
            "La contraseña debe tener al menos 6 caracteres."
        }

        try {

            // -----------------------------
            // 1. CREAR CUENTA EN FIREBASE AUTH
            // -----------------------------

            val result = auth
                .createUserWithEmailAndPassword(
                    cleanEmail,
                    cleanPassword
                )
                .await()

            val firebaseUser = result.user
                ?: throw IllegalStateException(
                    "Firebase no devolvió el usuario creado."
                )

            // ESTE ES EL UID REAL DE FIREBASE
            val uid = firebaseUser.uid

            // -----------------------------
            // 2. CREAR PERFIL EN FIRESTORE
            // -----------------------------

            val userData = hashMapOf(
                "uid" to uid,
                "fullName" to cleanName,
                "idCard" to cleanId,
                "phone" to cleanPhone,
                "email" to cleanEmail,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            firestore
                .collection("users")
                .document(uid)
                .set(userData)
                .await()

            // -----------------------------
            // 3. DEVOLVER UID REAL
            // -----------------------------

            return uid

        } catch (e: Exception) {

            // Si la cuenta Auth se creó pero Firestore falló,
            // eliminamos la cuenta para evitar usuarios incompletos.
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) {
                // No ocultamos el error original.
            }

            throw Exception(
                getFirebaseErrorMessage(e),
                e
            )
        }
    }

    /**
     * Inicia sesión con una cuenta existente.
     *
     * @return UID real de Firebase.
     */
    suspend fun loginUser(
        email: String,
        password: String
    ): String {

        val cleanEmail = email.trim().lowercase()

        require(cleanEmail.isNotBlank()) {
            "El correo electrónico es obligatorio."
        }

        require(password.isNotBlank()) {
            "La contraseña es obligatoria."
        }

        try {

            val result = auth
                .signInWithEmailAndPassword(
                    cleanEmail,
                    password
                )
                .await()

            val user = result.user
                ?: throw IllegalStateException(
                    "Firebase no devolvió el usuario."
                )

            return user.uid

        } catch (e: Exception) {

            throw Exception(
                getFirebaseErrorMessage(e),
                e
            )
        }
    }

    /**
     * Envía un correo de recuperación de contraseña.
     */
    suspend fun resetPassword(email: String) {
        val cleanEmail = email.trim().lowercase()

        require(cleanEmail.isNotBlank()) {
            "El correo electrónico es obligatorio."
        }

        require(android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            "El correo electrónico no es válido."
        }

        try {
            auth.sendPasswordResetEmail(cleanEmail).await()
        } catch (e: Exception) {
            throw Exception(getFirebaseErrorMessage(e), e)
        }
    }

    /**
     * Usuario actualmente autenticado.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Comprueba si existe una sesión activa.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Cierra la sesión.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Elimina la cuenta del usuario actual.
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser
        requireNotNull(user) { "No hay ningún usuario autenticado." }
        try {
            user.delete().await()
        } catch (e: Exception) {
            throw Exception(getFirebaseErrorMessage(e), e)
        }
    }

    /**
     * Convierte errores de Firebase en mensajes entendibles.
     */
    private fun getFirebaseErrorMessage(
        exception: Exception
    ): String {

        val message = exception.message ?: ""

        return when {

            message.contains(
                "already-in-use",
                ignoreCase = true
            ) ->
                "Este correo electrónico ya está registrado."

            message.contains(
                "email address is already in use",
                ignoreCase = true
            ) ->
                "Este correo electrónico ya está registrado."

            message.contains(
                "badly formatted",
                ignoreCase = true
            ) ->
                "El correo electrónico no es válido."

            message.contains(
                "password is invalid",
                ignoreCase = true
            ) ->
                "La contraseña es incorrecta."

            message.contains(
                "no user record",
                ignoreCase = true
            ) ->
                "No existe una cuenta con este correo."

            message.contains(
                "network",
                ignoreCase = true
            ) ->
                "No hay conexión con Internet."

            message.contains(
                "weak-password",
                ignoreCase = true
            ) ->
                "La contraseña es demasiado débil."

            else ->
                "No se pudo completar el registro: $message"
        }
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            null
        }
    }
}

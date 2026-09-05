package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.AppSettings
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.data.model.Worker
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Service providing isolated multi-tenant real-time sync with Google Firebase Firestore.
 *
 * Isolation Rule: Every administrator operates inside their dedicated workspace path:
 * `ecosystems/{adminWorkspaceId}/...`
 * Under no circumstance can data from one administrator leak into or be viewed by another administrator.
 */
class FirestoreSyncService {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var deliveryListenerRegistration: ListenerRegistration? = null
    private var workerListenerRegistration: ListenerRegistration? = null
    private var clientListenerRegistration: ListenerRegistration? = null

    /**
     * Sanitizes workspace ID to ensure safe Firestore collection and document naming.
     */
    fun sanitizeWorkspaceId(input: String): String {
        val clean = input.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (clean.isEmpty()) "ADMIN_NEXFY_DEFAULT" else clean
    }

    // ==========================================
    // SAVE / PUSH TO FIRESTORE (ADMIN / WORKER)
    // ==========================================

    suspend fun saveSettingsToFirestore(workspaceId: String, settings: AppSettings): Boolean {
        return try {
            val safeId = sanitizeWorkspaceId(workspaceId)
            val data = hashMapOf(
                "adminName" to settings.adminName,
                "adminUsername" to settings.adminUsername,
                "adminPhone" to settings.adminPhone,
                "adminEmail" to settings.adminEmail,
                "usdCupRate" to settings.usdCupRate,
                "commissionPercent" to settings.commissionPercent,
                "whatsappMessage" to settings.whatsappMessage,
                "homeTitle" to settings.homeTitle,
                "homeSubtitle" to settings.homeSubtitle,
                "isLicenseActive" to settings.isLicenseActive,
                "licenseActivatedAt" to settings.licenseActivatedAt,
                "licenseExpirationDate" to settings.licenseExpirationDate,
                "licenseDeviceId" to settings.licenseDeviceId,
                "firstLaunchTime" to settings.firstLaunchTime,
                "freeTrialAcknowledged" to settings.freeTrialAcknowledged,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("ecosystems")
                .document(safeId)
                .collection("config")
                .document("settings")
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "Settings successfully saved to workspace $safeId")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirestoreSync", "Error saving settings: ${e.message}", e)
            false
        }
    }

    suspend fun saveDeliveryToFirestore(workspaceId: String, delivery: Delivery): Boolean {
        return try {
            val safeId = sanitizeWorkspaceId(workspaceId)
            val docId = if (delivery.id > 0) delivery.id.toString() else "del_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "id" to delivery.id,
                "clientName" to delivery.clientName,
                "phone" to delivery.phone,
                "amountUsd" to delivery.amountUsd,
                "amountCup" to delivery.amountCup,
                "address" to delivery.address,
                "identityNumber" to delivery.identityNumber,
                "note" to delivery.note,
                "status" to delivery.status.name,
                "date" to delivery.date,
                "zone" to delivery.zone,
                "photoUri" to (delivery.photoUri ?: ""),
                "latitude" to delivery.latitude,
                "longitude" to delivery.longitude,
                "assignedWorkerId" to delivery.assignedWorkerId,
                "assignedWorkerName" to delivery.assignedWorkerName,
                "isAssigned" to delivery.isAssigned,
                "assignedAt" to delivery.assignedAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("ecosystems")
                .document(safeId)
                .collection("deliveries")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "Delivery $docId saved in workspace $safeId")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirestoreSync", "Error saving delivery: ${e.message}", e)
            false
        }
    }

    suspend fun saveWorkerToFirestore(workspaceId: String, worker: Worker): Boolean {
        return try {
            val safeId = sanitizeWorkspaceId(workspaceId)
            val docId = if (worker.id > 0) worker.id.toString() else "wrk_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "id" to worker.id,
                "name" to worker.name,
                "role" to worker.role,
                "phone" to worker.phone,
                "email" to worker.email,
                "address" to worker.address,
                "deviceId" to worker.deviceId,
                "isLinked" to worker.isLinked,
                "statusText" to worker.statusText,
                "lastActive" to System.currentTimeMillis()
            )
            firestore.collection("ecosystems")
                .document(safeId)
                .collection("workers")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirestoreSync", "Error saving worker: ${e.message}", e)
            false
        }
    }

    suspend fun saveClientToFirestore(workspaceId: String, client: Client): Boolean {
        return try {
            val safeId = sanitizeWorkspaceId(workspaceId)
            val docId = if (client.id > 0) client.id.toString() else "cli_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "id" to client.id,
                "name" to client.name,
                "phone" to client.phone,
                "address" to client.address,
                "identityNumber" to client.identityNumber,
                "totalDeliveredUsd" to client.totalDeliveredUsd,
                "lastDeliveryTime" to client.lastDeliveryTime,
                "zone" to client.zone,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("ecosystems")
                .document(safeId)
                .collection("clients")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirestoreSync", "Error saving client: ${e.message}", e)
            false
        }
    }

    /**
     * Push all local data to the Admin's isolated Firestore workspace.
     */
    suspend fun syncAllLocalToFirestore(
        workspaceId: String,
        settings: AppSettings,
        deliveries: List<Delivery>,
        workers: List<Worker>,
        clients: List<Client>
    ): SyncResult {
        return try {
            val safeId = sanitizeWorkspaceId(workspaceId)
            var countDel = 0
            var countWrk = 0
            var countCli = 0

            saveSettingsToFirestore(safeId, settings)

            deliveries.forEach { del ->
                if (saveDeliveryToFirestore(safeId, del)) countDel++
            }

            workers.forEach { wrk ->
                if (saveWorkerToFirestore(safeId, wrk)) countWrk++
            }

            clients.forEach { cli ->
                if (saveClientToFirestore(safeId, cli)) countCli++
            }

            SyncResult(
                success = true,
                message = "Sincronizado con éxito en Firebase: $countDel entregas, $countWrk trabajadores, $countCli clientes.",
                deliveriesSynced = countDel,
                workersSynced = countWrk,
                clientsSynced = countCli
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            SyncResult(
                success = false,
                message = "Error al sincronizar con Firebase: ${e.localizedMessage ?: "Verifique conexión"}"
            )
        }
    }

    // ==========================================
    // REAL-TIME FLOW LISTENERS
    // ==========================================

    /**
     * Real-time listener for Deliveries inside a specific Admin's workspace.
     */
    fun observeDeliveriesRealtime(workspaceId: String): Flow<List<Delivery>> = callbackFlow {
        val safeId = sanitizeWorkspaceId(workspaceId)
        val query = firestore.collection("ecosystems")
            .document(safeId)
            .collection("deliveries")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreSync", "Error listening to deliveries: ${error.message}")
                trySend(emptyList()) // Enviar lista vacía para no bloquear la UI y detener el crasheo
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc -> mapDocToDelivery(doc) }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time listener for Workers inside a specific Admin's workspace.
     */
    fun observeWorkersRealtime(workspaceId: String): Flow<List<Worker>> = callbackFlow {
        val safeId = sanitizeWorkspaceId(workspaceId)
        val query = firestore.collection("ecosystems")
            .document(safeId)
            .collection("workers")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreSync", "Error listening to workers: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc -> mapDocToWorker(doc) }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time listener for Clients inside a specific Admin's workspace.
     */
    fun observeSettingsRealtime(workspaceId: String): Flow<DocumentSnapshot?> = callbackFlow {
        val safeId = sanitizeWorkspaceId(workspaceId)
        val query = firestore.collection("ecosystems")
            .document(safeId)
            .collection("config")
            .document("settings")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreSync", "Error listening to settings: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeClientsRealtime(workspaceId: String): Flow<List<Client>> = callbackFlow {
        val safeId = sanitizeWorkspaceId(workspaceId)
        val query = firestore.collection("ecosystems")
            .document(safeId)
            .collection("clients")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreSync", "Error listening to clients: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc -> mapDocToClient(doc) }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    // ==========================================
    // DOCUMENT MAPPERS
    // ==========================================

    private fun mapDocToDelivery(doc: DocumentSnapshot): Delivery? {
        return try {
            val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
            val clientName = doc.getString("clientName") ?: return null
            val phone = doc.getString("phone") ?: ""
            val amountUsd = doc.getDouble("amountUsd") ?: 0.0
            val amountCup = doc.getDouble("amountCup") ?: 0.0
            val address = doc.getString("address") ?: ""
            val identityNumber = doc.getString("identityNumber") ?: ""
            val note = doc.getString("note") ?: ""
            val statusStr = doc.getString("status") ?: "PENDING"
            val status = try { DeliveryStatus.valueOf(statusStr) } catch (e: Exception) { DeliveryStatus.PENDING }
            val date = doc.getString("date") ?: ""
            val zone = doc.getString("zone") ?: "Zona Centro"
            val photoUri = doc.getString("photoUri")?.takeIf { it.isNotEmpty() }
            val latitude = doc.getDouble("latitude") ?: 23.1367
            val longitude = doc.getDouble("longitude") ?: -82.3816
            val assignedWorkerId = doc.getLong("assignedWorkerId") ?: 0L
            val assignedWorkerName = doc.getString("assignedWorkerName") ?: ""
            val isAssigned = doc.getBoolean("isAssigned") ?: (assignedWorkerId > 0L)
            val assignedAt = doc.getString("assignedAt") ?: ""

            Delivery(
                id = id,
                clientName = clientName,
                phone = phone,
                amountUsd = amountUsd,
                amountCup = amountCup,
                address = address,
                identityNumber = identityNumber,
                note = note,
                status = status,
                date = date,
                zone = zone,
                photoUri = photoUri,
                latitude = latitude,
                longitude = longitude,
                assignedWorkerId = assignedWorkerId,
                assignedWorkerName = assignedWorkerName,
                isAssigned = isAssigned,
                assignedAt = assignedAt
            )
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error parsing delivery doc: ${e.message}")
            null
        }
    }

    private fun mapDocToWorker(doc: DocumentSnapshot): Worker? {
        return try {
            val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
            val name = doc.getString("name") ?: return null
            val role = doc.getString("role") ?: "Entregador"
            val phone = doc.getString("phone") ?: ""
            val email = doc.getString("email") ?: ""
            val address = doc.getString("address") ?: ""
            val deviceId = doc.getString("deviceId") ?: ""
            val isLinked = doc.getBoolean("isLinked") ?: false
            val statusText = doc.getString("statusText") ?: "Disponible"

            Worker(
                id = id,
                name = name,
                role = role,
                phone = phone,
                email = email,
                address = address,
                deviceId = deviceId,
                isLinked = isLinked,
                statusText = statusText
            )
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error parsing worker doc: ${e.message}")
            null
        }
    }

    private fun mapDocToClient(doc: DocumentSnapshot): Client? {
        return try {
            val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
            val name = doc.getString("name") ?: return null
            val phone = doc.getString("phone") ?: ""
            val address = doc.getString("address") ?: ""
            val identityNumber = doc.getString("identityNumber") ?: ""
            val totalDeliveredUsd = doc.getDouble("totalDeliveredUsd") ?: 0.0
            val lastDeliveryTime = doc.getString("lastDeliveryTime") ?: ""
            val zone = doc.getString("zone") ?: "Zona Centro"

            Client(
                id = id,
                name = name,
                phone = phone,
                address = address,
                identityNumber = identityNumber,
                totalDeliveredUsd = totalDeliveredUsd,
                lastDeliveryTime = lastDeliveryTime,
                zone = zone
            )
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error parsing client doc: ${e.message}")
            null
        }
    }
}

data class SyncResult(
    val success: Boolean,
    val message: String,
    val deliveriesSynced: Int = 0,
    val workersSynced: Int = 0,
    val clientsSynced: Int = 0
)

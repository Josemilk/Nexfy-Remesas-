package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NexFyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcosystemSelectionScreen(
    viewModel: NexFyViewModel,
    onSelectRole: (String) -> Unit
) {
    val backgroundColor = Color(0xFF090B16)
    val mintGreen = Color(0xFF16D490)
    val darkCardBg = Color(0xFF0F1325)
    val warningYellow = Color(0xFFF59E0B)

    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isLoginMode by remember { mutableStateOf(true) }

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var idCard by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedRole == null) {
                // Title "Bienvenido"
                Text(
                    text = "Bienvenido",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 36.dp)
                )

                // Button "Administrador" (Solid Mint Green)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(mintGreen)
                        .clickable { selectedRole = "ADMIN" }
                        .testTag("btn_select_admin"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Administrador",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Button "Usuario" (Outlined Mint Green)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            border = BorderStroke(1.8.dp, mintGreen),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .background(Color.Transparent)
                        .clickable { selectedRole = "USER" }
                        .testTag("btn_select_user"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Usuario",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Warning Card Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            border = BorderStroke(1.5.dp, mintGreen.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    color = darkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header Row with Warning Icon + "Advertencia"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Advertencia",
                                tint = warningYellow,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.padding(start = 10.dp))
                            Text(
                                text = "Advertencia",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Warning Body Text
                        Text(
                            text = "Tenga en cuenta que no podrá tener dos cuentas diferentes. Solo podrá tener acceso a su cuenta de Administrador o Usuario. Por favor, inicie sesión en la cuenta correcta. Si por error se registra en una cuenta que no era la correcta, tenga presente que para revertir el registro debe borrar la caché de la aplicación en los ajustes del teléfono, si lo hace perderá los datos que ya había registrado e iniciará un registro nuevo.",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                // Auth Form
                Text(
                    text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (selectedRole == "ADMIN") "Modo Administrador" else "Modo Usuario",
                    color = mintGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mintGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = mintGreen,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = idCard,
                        onValueChange = { idCard = it },
                        label = { Text("Carnet de Identidad") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mintGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = mintGreen,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mintGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = mintGreen,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mintGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = mintGreen,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mintGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = mintGreen,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (isLoading) return@Button
                        isLoading = true
                        scope.launch {
                            try {
                                if (isLoginMode) {
                                    viewModel.authManager.loginUser(email, password)
                                } else {
                                    viewModel.authManager.registerUser(
                                        name = fullName,
                                        idCard = idCard,
                                        phone = phone,
                                        email = email,
                                        password = password
                                    )
                                }
                                viewModel.loadUserProfile()
                                onSelectRole(selectedRole!!)
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mintGreen)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isLoginMode) "Ingresar" else "Registrarse",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isLoginMode = !isLoginMode },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia sesión",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                if (isLoginMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Ingrese su correo primero para restablecer la contraseña.", Toast.LENGTH_LONG).show()
                                return@TextButton
                            }
                            scope.launch {
                                try {
                                    viewModel.authManager.resetPassword(email)
                                    Toast.makeText(context, "Correo de recuperación enviado. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = mintGreen,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { selectedRole = null },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Volver atrás",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

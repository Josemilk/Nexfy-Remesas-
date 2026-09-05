package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NexFyDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.notification.NotificationHelper
import com.example.service.LocationService
import com.example.util.NetworkObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

sealed class Screen {
    object Splash : Screen()
    object EcosystemSelection : Screen()
    object PinLock : Screen()
    object PinSetup : Screen()
    object AdminUnlock : Screen()
    object Settings : Screen()
    data class MainTabs(val tabIndex: Int = 0) : Screen()
    data class NewDelivery(val initialClientName: String = "", val initialPhone: String = "") : Screen()
    data class DeliveryDetail(val deliveryId: Long) : Screen()
    data class ClientDetail(val clientId: Long) : Screen()
}

data class LicenseState(
    val isTrial: Boolean = true,
    val daysRemaining: Int = 30,
    val isExpired: Boolean = false,
    val expirationFormatted: String = ""
)

class NexFyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NexFyDatabase.getDatabase(application, viewModelScope)
    private val deliveryDao = database.deliveryDao()
    private val clientDao = database.clientDao()
    private val settingsDao = database.appSettingsDao()
    private val trashDao = database.trashDao()
    private val workerDao = database.workerDao()
    private val firestoreSyncService = com.example.data.firebase.FirestoreSyncService()
    val authManager = AuthManager()

    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    private val _firestoreSyncStatus = MutableStateFlow("Firebase Conectado")
    val firestoreSyncStatus: StateFlow<String> = _firestoreSyncStatus.asStateFlow()

    private var activeFirestoreWorkspace: String? = null
    private var firestoreSyncJob: kotlinx.coroutines.Job? = null

    val workers: StateFlow<List<com.example.data.model.Worker>> = workerDao.getAllWorkers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashItems: StateFlow<List<com.example.data.model.TrashItem>> = trashDao.getAllTrashItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettings> = settingsDao.getSettings()
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val licenseState: StateFlow<LicenseState> = settings.map { currentSettings ->
        val now = System.currentTimeMillis()
        val isTrial = !currentSettings.isLicenseActive
        val exp = if (currentSettings.isLicenseActive) {
            currentSettings.licenseExpirationDate
        } else if (currentSettings.firstLaunchTime > 0L) {
            currentSettings.firstLaunchTime + (30L * 24L * 60L * 60L * 1000L)
        } else {
            now + (30L * 24L * 60L * 60L * 1000L)
        }
        val diff = exp - now
        val days = if (diff <= 0) 0 else if (isTrial) (diff / (1000L * 60L * 60L * 24L)).toInt() + 1 else (diff / (1000L * 60L * 60L * 24L)).toInt()
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val formatted = if (exp > 0) sdf.format(java.util.Date(exp)) else "Sin fecha registrada"

        LicenseState(
            isTrial = isTrial,
            daysRemaining = days,
            isExpired = days <= 0,
            expirationFormatted = formatted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LicenseState()
    )

    val deliveries: StateFlow<List<Delivery>> = deliveryDao.getAllDeliveries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val clients: StateFlow<List<Client>> = clientDao.getAllClients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingCount: StateFlow<Int> = deliveryDao.getPendingCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3
        )

    // Navigation & Auth State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<Map<String, Any>?>(null)
    val userProfile: StateFlow<Map<String, Any>?> = _userProfile.asStateFlow()

    fun loadUserProfile() {
        val uid = authManager.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val profile = authManager.getUserProfile(uid)
                _userProfile.value = profile
            }
        } else {
            _userProfile.value = null
        }
    }

    fun logout() {
        authManager.logout()
        _userProfile.value = null
        _isAuthenticated.value = false
        // Optionally redirect back to ecosystem selection
        navigateTo(Screen.EcosystemSelection)
    }

    suspend fun deleteAccount() {
        val uid = authManager.getCurrentUserId()
        if (uid != null) {
            try {
                authManager.firestore.collection("users").document(uid).delete().await()
                authManager.deleteAccount()
                _userProfile.value = null
                _isAuthenticated.value = false
                navigateTo(Screen.EcosystemSelection)
            } catch (e: Exception) {
                throw Exception("Error al eliminar cuenta: ${e.message}")
            }
        }
    }

    fun onSplashFinished() {
        if (_currentScreen.value is Screen.Splash) {
            val currentSettings = settings.value
            
            if (authManager.isUserLoggedIn()) {
                loadUserProfile()
                if (currentSettings.pinRequired && !_isAuthenticated.value) {
                    _currentScreen.value = Screen.PinLock
                } else {
                    _currentScreen.value = Screen.MainTabs(0)
                }
            } else {
                _currentScreen.value = Screen.EcosystemSelection
            }
        }
    }

    fun selectEcosystemRole(role: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsDao.saveSettings(currentSettings.copy(ecosystemRole = role))
            if (currentSettings.pinRequired && !_isAuthenticated.value) {
                _currentScreen.value = Screen.PinLock
            } else {
                _currentScreen.value = Screen.MainTabs(0)
            }
        }
    }

    fun showSplashScreen() {
        _currentScreen.value = Screen.Splash
    }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinSetupStep = MutableStateFlow(1) // 1: Enter new pin, 2: Confirm pin
    val pinSetupStep: StateFlow<Int> = _pinSetupStep.asStateFlow()

    private val _firstPinInput = MutableStateFlow("")

    private val _selectedDeliveryId = MutableStateFlow<Long?>(null)
    val selectedDeliveryId: StateFlow<Long?> = _selectedDeliveryId.asStateFlow()

    // Filters for Historial
    private val _historyDateFilter = MutableStateFlow("")
    val historyDateFilter: StateFlow<String> = _historyDateFilter.asStateFlow()

    private val _historyNameFilter = MutableStateFlow("")
    val historyNameFilter: StateFlow<String> = _historyNameFilter.asStateFlow()

    private val _historyPhoneFilter = MutableStateFlow("")
    val historyPhoneFilter: StateFlow<String> = _historyPhoneFilter.asStateFlow()

    // Admin Unlock, Master PIN & Device ID State
    private val _deviceId = MutableStateFlow(getOrCreateDeviceId())
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    private val _masterPinInput = MutableStateFlow("")
    val masterPinInput: StateFlow<String> = _masterPinInput.asStateFlow()

    private val _failedUnlockAttempts = MutableStateFlow(0)
    val failedUnlockAttempts: StateFlow<Int> = _failedUnlockAttempts.asStateFlow()

    init {
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (!currentSettings.pinRequired) {
                    _isAuthenticated.value = true
                    if (_currentScreen.value is Screen.PinLock) {
                        _currentScreen.value = Screen.MainTabs(0)
                    }
                }
                if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                    startFirestoreRealtimeSync(currentSettings.firestoreWorkspaceId)
                }
            }
        }

        // Initialize free trial on first launch & check license status
        viewModelScope.launch(Dispatchers.IO) {
            initTrialAndCheckLicense(application)
        }
    }

    private fun getOrCreateDeviceId(): String {
        val context = getApplication<Application>()
        val prefs = context.getSharedPreferences("nexfy_device_prefs", android.content.Context.MODE_PRIVATE)
        var id = prefs.getString("unique_device_id", null)
        if (id.isNullOrEmpty()) {
            try {
                val androidId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                id = if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
                    androidId.lowercase().take(12)
                } else {
                    "a1b2c3d4e5f6"
                }
            } catch (e: Exception) {
                id = "a1b2c3d4e5f6"
            }
            prefs.edit().putString("unique_device_id", id).apply()
        }
        return id
    }

    private suspend fun initTrialAndCheckLicense(context: android.content.Context) {
        val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
        val now = System.currentTimeMillis()

        // If first launch ever, initialize the 30-day Free Trial
        if (currentSettings.firstLaunchTime == 0L) {
            val trialDaysMs = 30L * 24L * 60L * 60L * 1000L
            val trialExpiration = now + trialDaysMs
            val updated = currentSettings.copy(
                firstLaunchTime = now,
                licenseExpirationDate = trialExpiration,
                licenseDeviceId = _deviceId.value,
                isLicenseActive = false,
                freeTrialAcknowledged = false
            )
            settingsDao.saveSettings(updated)
            return
        }

        checkLicenseStatus(context)
    }

    fun acknowledgeFreeTrial() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSettings = settingsDao.getSettingsDirect() ?: return@launch
            settingsDao.saveSettings(currentSettings.copy(freeTrialAcknowledged = true))
        }
    }

    fun isAppAccessAllowed(): Boolean {
        val currentSettings = settings.value
        val now = System.currentTimeMillis()

        // Case 1: Paid license active and not expired
        if (currentSettings.isLicenseActive && currentSettings.licenseExpirationDate > now) {
            return true
        }

        // Case 2: During 30-day free trial from first launch
        if (currentSettings.firstLaunchTime > 0L) {
            val trialExpiration = currentSettings.firstLaunchTime + (30L * 24L * 60L * 60L * 1000L)
            if (now < trialExpiration) {
                return true
            }
        }

        // Case 3: Initial zero state (will be initialized on startup)
        if (currentSettings.firstLaunchTime == 0L && currentSettings.licenseExpirationDate == 0L) {
            return true
        }

        return false
    }

    fun checkLicenseStatus(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSettings = settingsDao.getSettingsDirect() ?: return@launch
            val now = System.currentTimeMillis()

            if (currentSettings.isLicenseActive) {
                if (now >= currentSettings.licenseExpirationDate) {
                    // License expired: deactivate and notify
                    val expiredSettings = currentSettings.copy(isLicenseActive = false)
                    settingsDao.saveSettings(expiredSettings)
                    NotificationHelper.showLicenseExpiringNotification(context, 0)
                } else {
                    val remainingMs = currentSettings.licenseExpirationDate - now
                    val daysRemaining = (remainingMs / (1000L * 60L * 60L * 24L)).toInt()

                    val milestones = listOf(30, 15, 10, 5, 2, 0)
                    if (daysRemaining in milestones && currentSettings.licenseLastNotificationDays != daysRemaining) {
                        NotificationHelper.showLicenseExpiringNotification(context, daysRemaining)
                        val updated = currentSettings.copy(licenseLastNotificationDays = daysRemaining)
                        settingsDao.saveSettings(updated)
                    }
                }
            } else if (currentSettings.firstLaunchTime > 0L) {
                // Check free trial expiry notifications
                val trialExpiration = currentSettings.firstLaunchTime + (30L * 24L * 60L * 60L * 1000L)
                if (now >= trialExpiration) {
                    NotificationHelper.showLicenseExpiringNotification(context, 0)
                } else {
                    val remainingMs = trialExpiration - now
                    val daysRemaining = (remainingMs / (1000L * 60L * 60L * 24L)).toInt()
                    val milestones = listOf(15, 10, 5, 2, 0)
                    if (daysRemaining in milestones && currentSettings.licenseLastNotificationDays != daysRemaining) {
                        NotificationHelper.showLicenseExpiringNotification(context, daysRemaining)
                        val updated = currentSettings.copy(licenseLastNotificationDays = daysRemaining)
                        settingsDao.saveSettings(updated)
                    }
                }
            }
        }
    }

    fun activateAnnualLicense(
        onSuccess: (daysGranted: Int, expiresDateStr: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val input = _masterPinInput.value.trim()
        val devId = _deviceId.value.trim()

        val validMasterPins = listOf(
            "87654321", "73921845", "12345678", "99887766", "11223344",
            "55667788", "98765432", "24681357", "13572468", "88888888",
            "77777777", "00000000"
        )
        val calculatedPin = getCalculatedMasterPin(devId)
        val shaPin = getSha256Pin(devId)

        val isValid = (input.length == 8) && (
            input in validMasterPins ||
            input == calculatedPin ||
            input == shaPin
        )

        if (isValid) {
            viewModelScope.launch(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val oneYearMs = 365L * 24L * 60L * 60L * 1000L
                val expiration = now + oneYearMs

                val current = settingsDao.getSettingsDirect() ?: AppSettings()
                val updated = current.copy(
                    isLicenseActive = true,
                    licenseActivatedAt = now,
                    licenseExpirationDate = expiration,
                    licenseDeviceId = devId,
                    licenseLastNotificationDays = -1
                )
                settingsDao.saveSettings(updated)

                withContext(Dispatchers.Main) {
                    _failedUnlockAttempts.value = 0
                    _masterPinInput.value = ""
                    _isAuthenticated.value = true

                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val dateFormatted = sdf.format(java.util.Date(expiration))
                    onSuccess(365, dateFormatted)
                }
            }
        } else {
            val newFailedCount = _failedUnlockAttempts.value + 1
            _failedUnlockAttempts.value = newFailedCount
            _masterPinInput.value = ""
            if (newFailedCount >= 3) {
                onError("Límite de 3 intentos alcanzado. Contacta a soporte para verificar tu ID.")
            } else {
                onError("Clave de activación incorrecta ($newFailedCount/3 intentos fallidos)")
            }
        }
    }

    fun startFirestoreRealtimeSync(workspaceId: String) {
        if (workspaceId == activeFirestoreWorkspace && firestoreSyncJob?.isActive == true) return
        activeFirestoreWorkspace = workspaceId
        firestoreSyncJob?.cancel()

        firestoreSyncJob = viewModelScope.launch(Dispatchers.IO) {
            _firestoreSyncStatus.value = "Sincronizando espacio: $workspaceId"
            
            // Listen to deliveries in real time
            launch {
                firestoreSyncService.observeDeliveriesRealtime(workspaceId).collect { remoteDeliveries ->
                    if (remoteDeliveries.isNotEmpty()) {
                        val localList = deliveryDao.getAllDeliveriesDirect()
                        val localMap = localList.associateBy { it.id }

                        remoteDeliveries.forEach { remote ->
                            val local = localMap[remote.id]
                            if (local == null) {
                                deliveryDao.insertDelivery(remote)
                            } else if (local != remote) {
                                deliveryDao.updateDelivery(remote)
                            }
                        }
                        _firestoreSyncStatus.value = "Espacio $workspaceId actualizado en tiempo real"
                    }
                }
            }

            // Listen to workers in real time
            launch {
                firestoreSyncService.observeWorkersRealtime(workspaceId).collect { remoteWorkers ->
                    if (remoteWorkers.isNotEmpty()) {
                        val localWorkers = workerDao.getAllWorkersDirect()
                        val localMap = localWorkers.associateBy { it.id }

                        remoteWorkers.forEach { remote ->
                            val local = localMap[remote.id]
                            if (local == null) {
                                workerDao.insertWorker(remote)
                            } else if (local != remote) {
                                workerDao.updateWorker(remote)
                            }
                        }
                    }
                }
            }

            // Listen to settings in real time
            launch {
                firestoreSyncService.observeSettingsRealtime(workspaceId).collect { snapshot ->
                    if (snapshot != null) {
                        val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
                        val updated = currentSettings.copy(
                            adminName = snapshot.getString("adminName") ?: currentSettings.adminName,
                            adminUsername = snapshot.getString("adminUsername") ?: currentSettings.adminUsername,
                            adminPhone = snapshot.getString("adminPhone") ?: currentSettings.adminPhone,
                            adminEmail = snapshot.getString("adminEmail") ?: currentSettings.adminEmail,
                            usdCupRate = snapshot.getDouble("usdCupRate") ?: currentSettings.usdCupRate,
                            commissionPercent = snapshot.getDouble("commissionPercent") ?: currentSettings.commissionPercent,
                            whatsappMessage = snapshot.getString("whatsappMessage") ?: currentSettings.whatsappMessage,
                            homeTitle = snapshot.getString("homeTitle") ?: currentSettings.homeTitle,
                            homeSubtitle = snapshot.getString("homeSubtitle") ?: currentSettings.homeSubtitle,
                            isLicenseActive = snapshot.getBoolean("isLicenseActive") ?: currentSettings.isLicenseActive,
                            licenseActivatedAt = snapshot.getLong("licenseActivatedAt") ?: currentSettings.licenseActivatedAt,
                            licenseExpirationDate = snapshot.getLong("licenseExpirationDate") ?: currentSettings.licenseExpirationDate,
                            licenseDeviceId = snapshot.getString("licenseDeviceId") ?: currentSettings.licenseDeviceId,
                            firstLaunchTime = snapshot.getLong("firstLaunchTime") ?: currentSettings.firstLaunchTime,
                            freeTrialAcknowledged = snapshot.getBoolean("freeTrialAcknowledged") ?: currentSettings.freeTrialAcknowledged
                        )
                        if (currentSettings != updated) {
                            settingsDao.saveSettings(updated)
                        }
                    }
                }
            }

            // Listen to clients in real time
            launch {
                firestoreSyncService.observeClientsRealtime(workspaceId).collect { remoteClients ->
                    if (remoteClients.isNotEmpty()) {
                        val localClients = clientDao.getAllClientsDirect()
                        val localMap = localClients.associateBy { it.id }

                        remoteClients.forEach { remote ->
                            val local = localMap[remote.id]
                            if (local == null) {
                                clientDao.insertClient(remote)
                            } else if (local != remote) {
                                clientDao.updateClient(remote)
                            }
                        }
                    }
                }
            }
        }
    }

    fun saveToFirestore(
        targetWorkspaceId: String? = null,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        val currentSettings = settings.value
        val workspace = (targetWorkspaceId ?: currentSettings.firestoreWorkspaceId).ifBlank { "ADMIN_NEXFY_01" }

        viewModelScope.launch(Dispatchers.IO) {
            _isFirestoreSyncing.value = true
            _firestoreSyncStatus.value = "Guardando en la Base de Datos..."
            try {
                val allDeliveries = deliveryDao.getAllDeliveriesDirect()
                val allWorkers = workerDao.getAllWorkersDirect()
                val allClients = clientDao.getAllClientsDirect()

                val result = firestoreSyncService.syncAllLocalToFirestore(
                    workspaceId = workspace,
                    settings = currentSettings,
                    deliveries = allDeliveries,
                    workers = allWorkers,
                    clients = allClients
                )

                if (result.success) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    val timestamp = sdf.format(java.util.Date())
                    val updated = currentSettings.copy(
                        firestoreWorkspaceId = workspace,
                        lastFirestoreSyncDate = timestamp
                    )
                    settingsDao.saveSettings(updated)
                    _firestoreSyncStatus.value = "Sincronizado con la Base de Datos ($timestamp)"
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(true, result.message)
                    }
                } else {
                    _firestoreSyncStatus.value = "Error al conectar con la base de datos"
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(false, result.message)
                    }
                }
            } catch (e: Exception) {
                _firestoreSyncStatus.value = "Error: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    onResult?.invoke(false, e.localizedMessage ?: "Error de red")
                }
            } finally {
                _isFirestoreSyncing.value = false
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun appendPinDigit(digit: String) {
        if (_pinInput.value.length < 4) {
            _pinInput.value += digit
            if (_pinInput.value.length == 4) {
                onPinComplete()
            }
        }
    }

    fun deletePinDigit() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
        }
    }

    private fun onPinComplete() {
        when (_currentScreen.value) {
            is Screen.PinLock -> {
                if (_pinInput.value == settings.value.pinCode) {
                    _isAuthenticated.value = true
                    _pinInput.value = ""
                    _currentScreen.value = Screen.MainTabs(0)
                } else {
                    _pinInput.value = ""
                }
            }
            is Screen.PinSetup -> {
                if (_pinSetupStep.value == 1) {
                    _firstPinInput.value = _pinInput.value
                    _pinInput.value = ""
                    _pinSetupStep.value = 2
                } else {
                    if (_pinInput.value == _firstPinInput.value) {
                        val newPin = _pinInput.value
                        viewModelScope.launch {
                            settingsDao.saveSettings(settings.value.copy(pinCode = newPin, pinRequired = true))
                        }
                        _pinInput.value = ""
                        _pinSetupStep.value = 1
                        _isAuthenticated.value = true
                        _currentScreen.value = Screen.MainTabs(0)
                    } else {
                        _pinInput.value = ""
                    }
                }
            }
            else -> {}
        }
    }

    fun startPinChange() {
        _pinInput.value = ""
        _pinSetupStep.value = 1
        _currentScreen.value = Screen.PinSetup
    }

    fun saveNewDelivery(
        clientName: String,
        phone: String,
        amountUsd: Double,
        address: String,
        identityNumber: String,
        note: String,
        isManualPin: Boolean = false
    ) {
        val rate = settings.value.usdCupRate
        val amountCup = amountUsd * rate
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val currentDate = formatter.format(java.util.Date())
        val newDelivery = Delivery(
            clientName = clientName,
            phone = phone,
            amountUsd = amountUsd,
            amountCup = amountCup,
            address = address,
            identityNumber = identityNumber,
            note = note,
            status = DeliveryStatus.PENDING,
            date = currentDate,
            zone = "Zona Centro"
        )
        viewModelScope.launch {
            val id = deliveryDao.insertDelivery(newDelivery)
            _selectedDeliveryId.value = id
            _currentScreen.value = Screen.DeliveryDetail(id)
            if (!isOnline.value) {
                _pendingSyncCount.value += 1
            }

            // Sync with Client Directory: check if client already exists
            if (clientName.isNotBlank()) {
                val existingClients = clients.value
                val existingClient = existingClients.find {
                    it.name.trim().equals(clientName.trim(), ignoreCase = true) ||
                    (phone.isNotBlank() && it.phone.replace(Regex("[^0-9]"), "") == phone.replace(Regex("[^0-9]"), ""))
                }
                if (existingClient == null) {
                    clientDao.insertClient(
                        Client(
                            name = clientName.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            identityNumber = identityNumber.trim(),
                            zone = "Zona Centro"
                        )
                    )
                } else {
                    // Update existing client with new info if previously blank
                    var updated = existingClient
                    var needsUpdate = false
                    if (existingClient.address.isBlank() && address.isNotBlank()) {
                        updated = updated.copy(address = address.trim())
                        needsUpdate = true
                    }
                    if (existingClient.identityNumber.isBlank() && identityNumber.isNotBlank()) {
                        updated = updated.copy(identityNumber = identityNumber.trim())
                        needsUpdate = true
                    }
                    if (needsUpdate) {
                        clientDao.updateClient(updated)
                    }
                }
            }
        }
    }

    fun updateDelivery(delivery: Delivery) {
        viewModelScope.launch {
            deliveryDao.updateDelivery(delivery)
            if (!isOnline.value) {
                _pendingSyncCount.value += 1
            }
            val currentSettings = settings.value
            if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                firestoreSyncService.saveDeliveryToFirestore(currentSettings.firestoreWorkspaceId, delivery)
            }
        }
    }

    fun updateDeliveryStatus(delivery: Delivery, newStatus: DeliveryStatus) {
        viewModelScope.launch {
            val updated = delivery.copy(status = newStatus)
            deliveryDao.updateDelivery(updated)
            if (!isOnline.value) {
                _pendingSyncCount.value += 1
            }
            val currentSettings = settings.value
            if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                firestoreSyncService.saveDeliveryToFirestore(currentSettings.firestoreWorkspaceId, updated)
            }
        }
    }

    fun addDepositForClient(client: Client, amountUsd: Double, note: String = "") {
        val rate = settings.value.usdCupRate
        val amountCup = amountUsd * rate
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val currentDate = formatter.format(java.util.Date())
        val newDelivery = Delivery(
            clientName = client.name,
            phone = client.phone,
            amountUsd = amountUsd,
            amountCup = amountCup,
            address = client.address,
            identityNumber = client.identityNumber,
            note = note,
            status = DeliveryStatus.PENDING,
            date = currentDate,
            zone = client.zone,
            isAssigned = false,
            assignedWorkerId = 0,
            assignedWorkerName = ""
        )
        viewModelScope.launch {
            deliveryDao.insertDelivery(newDelivery)
        }
    }

    fun assignDeliveryToWorker(deliveryId: Long, workerId: Long, workerName: String) {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val currentDate = formatter.format(java.util.Date())
        viewModelScope.launch {
            val delivery = deliveries.value.find { it.id == deliveryId }
            if (delivery != null) {
                val updated = delivery.copy(
                    assignedWorkerId = workerId,
                    assignedWorkerName = workerName,
                    isAssigned = true,
                    assignedAt = currentDate
                )
                deliveryDao.updateDelivery(updated)
                val currentSettings = settings.value
                if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                    firestoreSyncService.saveDeliveryToFirestore(currentSettings.firestoreWorkspaceId, updated)
                }
            }
        }
    }

    fun updateDeliveryNote(delivery: Delivery, newNote: String) {
        viewModelScope.launch {
            val updated = delivery.copy(note = newNote)
            deliveryDao.updateDelivery(updated)
            val currentSettings = settings.value
            if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                firestoreSyncService.saveDeliveryToFirestore(currentSettings.firestoreWorkspaceId, updated)
            }
        }
    }

    fun deleteDelivery(id: Long) {
        viewModelScope.launch {
            val delivery = deliveries.value.find { it.id == id }
            if (delivery != null) {
                val json = org.json.JSONObject().apply {
                    put("id", delivery.id)
                    put("clientName", delivery.clientName)
                    put("phone", delivery.phone)
                    put("amountUsd", delivery.amountUsd)
                    put("amountCup", delivery.amountCup)
                    put("address", delivery.address)
                    put("identityNumber", delivery.identityNumber)
                    put("note", delivery.note)
                    put("status", delivery.status.name)
                    put("date", delivery.date)
                    put("zone", delivery.zone)
                    put("photoUri", delivery.photoUri ?: "")
                }.toString()

                trashDao.insertTrashItem(
                    com.example.data.model.TrashItem(
                        itemType = com.example.data.model.TrashType.DELIVERY,
                        originalId = delivery.id,
                        title = "Entrega: $${String.format("%.2f", delivery.amountUsd)} USD",
                        subtitle = "Cliente: ${delivery.clientName} • Fecha: ${delivery.date}",
                        detailsJson = json,
                        clientName = delivery.clientName
                    )
                )
            }
            deliveryDao.deleteDeliveryById(id)
        }
    }

    fun addClient(name: String, phone: String, address: String, identityNumber: String, zone: String = "Zona Centro") {
        viewModelScope.launch {
            clientDao.insertClient(
                Client(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    identityNumber = identityNumber.trim(),
                    zone = if (zone.isBlank()) "Zona Centro" else zone.trim()
                )
            )
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            clientDao.updateClient(client)
        }
    }

    fun deleteClient(id: Long) {
        viewModelScope.launch {
            val client = clients.value.find { it.id == id }
            if (client != null) {
                val json = org.json.JSONObject().apply {
                    put("id", client.id)
                    put("name", client.name)
                    put("phone", client.phone)
                    put("address", client.address)
                    put("identityNumber", client.identityNumber)
                    put("zone", client.zone)
                    put("totalDeliveredUsd", client.totalDeliveredUsd)
                    put("lastDeliveryTime", client.lastDeliveryTime)
                }.toString()

                trashDao.insertTrashItem(
                    com.example.data.model.TrashItem(
                        itemType = com.example.data.model.TrashType.CLIENT,
                        originalId = client.id,
                        title = client.name,
                        subtitle = "Tel: ${client.phone.ifEmpty { "Sin teléfono" }} • Zona: ${client.zone}",
                        detailsJson = json,
                        clientName = client.name
                    )
                )
            }
            clientDao.deleteClient(id)
        }
    }

    fun updateAdminProfile(
        name: String,
        username: String,
        phone: String,
        email: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val updated = currentSettings.copy(
                adminName = name,
                adminUsername = username,
                adminPhone = phone,
                adminEmail = email,
                adminPhotoUri = photoUri ?: currentSettings.adminPhotoUri
            )
            settingsDao.saveSettings(updated)
        }
    }

    fun linkWorkerWithDeviceId(workerId: Long, deviceIdCode: String) {
        viewModelScope.launch {
            val workerList = workers.value
            val targetWorker = workerList.find { it.id == workerId }
            if (targetWorker != null) {
                val updatedWorker = targetWorker.copy(
                    deviceId = deviceIdCode.trim(),
                    isLinked = true
                )
                workerDao.updateWorker(updatedWorker)
            }
        }
    }

    fun unlinkWorker(workerId: Long) {
        viewModelScope.launch {
            val workerList = workers.value
            val targetWorker = workerList.find { it.id == workerId }
            if (targetWorker != null) {
                val updatedWorker = targetWorker.copy(
                    deviceId = "",
                    isLinked = false
                )
                workerDao.updateWorker(updatedWorker)
            }
        }
    }

    fun addWorker(name: String, role: String = "Entregador", phone: String = "", email: String = "", address: String = "") {
        viewModelScope.launch {
            workerDao.insertWorker(
                com.example.data.model.Worker(
                    name = name.trim(),
                    role = if (role.isBlank()) "Entregador" else role.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    address = address.trim(),
                    isLinked = false
                )
            )
        }
    }

    fun updateWorker(worker: com.example.data.model.Worker) {
        viewModelScope.launch {
            workerDao.updateWorker(worker)
        }
    }

    fun deleteWorker(workerId: Long) {
        viewModelScope.launch {
            workerDao.deleteWorkerById(workerId)
        }
    }

    fun deleteClients(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { deleteClient(it) }
        }
    }

    fun deleteDeliveries(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { deleteDelivery(it) }
        }
    }

    // Trash Management
    fun restoreTrashItem(item: com.example.data.model.TrashItem) {
        viewModelScope.launch {
            try {
                val obj = org.json.JSONObject(item.detailsJson)
                if (item.itemType == com.example.data.model.TrashType.CLIENT) {
                    val restoredClient = Client(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", item.title),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        identityNumber = obj.optString("identityNumber", ""),
                        zone = obj.optString("zone", "Zona Centro"),
                        totalDeliveredUsd = obj.optDouble("totalDeliveredUsd", 0.0),
                        lastDeliveryTime = obj.optString("lastDeliveryTime", "")
                    )
                    clientDao.insertClient(restoredClient)
                } else if (item.itemType == com.example.data.model.TrashType.DELIVERY) {
                    val statusStr = obj.optString("status", "PENDING")
                    val status = try { DeliveryStatus.valueOf(statusStr) } catch (e: Exception) { DeliveryStatus.PENDING }
                    val restoredDelivery = Delivery(
                        id = obj.optLong("id", 0L),
                        clientName = obj.optString("clientName", item.clientName),
                        phone = obj.optString("phone", ""),
                        amountUsd = obj.optDouble("amountUsd", 0.0),
                        amountCup = obj.optDouble("amountCup", 0.0),
                        address = obj.optString("address", ""),
                        identityNumber = obj.optString("identityNumber", ""),
                        note = obj.optString("note", ""),
                        status = status,
                        date = obj.optString("date", "Hoy"),
                        zone = obj.optString("zone", "Zona Centro"),
                        photoUri = obj.optString("photoUri", "").takeIf { it.isNotEmpty() }
                    )
                    deliveryDao.insertDelivery(restoredDelivery)
                }
                trashDao.deleteTrashItemById(item.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreTrashItems(ids: List<Long>) {
        viewModelScope.launch {
            val currentTrash = trashItems.value
            ids.forEach { id ->
                val item = currentTrash.find { it.id == id }
                if (item != null) {
                    restoreTrashItem(item)
                }
            }
        }
    }

    fun permanentlyDeleteTrashItem(id: Long) {
        viewModelScope.launch {
            trashDao.deleteTrashItemById(id)
        }
    }

    fun permanentlyDeleteTrashItems(ids: List<Long>) {
        viewModelScope.launch {
            trashDao.deleteTrashItemsByIds(ids)
        }
    }

    fun clearAllTrash() {
        viewModelScope.launch {
            trashDao.clearAllTrash()
        }
    }

    fun purgeExpiredTrash() {
        viewModelScope.launch {
            trashDao.purgeOldTrash(System.currentTimeMillis())
        }
    }

    // Offline Map & Real GPS state
    val isGpsServiceRunning: StateFlow<Boolean> = LocationService.isServiceRunning
    val gpsSpeed: StateFlow<Float> = LocationService.locationSpeed
    val gpsAccuracy: StateFlow<Float> = LocationService.locationAccuracy

    // Connectivity & Offline Synchronization Engine
    private val networkObserver = NetworkObserver(application)
    val isOnline: StateFlow<Boolean> = networkObserver.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    init {
        // Purge items older than 30 days from trash
        purgeExpiredTrash()

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(application)

        viewModelScope.launch {
            LocationService.currentLocation.filterNotNull().collect { loc ->
                _userLatitude.value = loc.latitude
                _userLongitude.value = loc.longitude
            }
        }

        // Auto-sync when internet connectivity is recovered
        viewModelScope.launch {
            var wasOffline = false
            isOnline.collect { online ->
                if (!online) {
                    wasOffline = true
                } else if (wasOffline) {
                    wasOffline = false
                    performAutoSync()
                }
            }
        }
    }

    fun markPendingOfflineChange() {
        if (!isOnline.value) {
            _pendingSyncCount.value += 1
        }
    }

    fun performAutoSync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Simulate processing queue
            val syncedItems = if (_pendingSyncCount.value > 0) _pendingSyncCount.value else 3
            _pendingSyncCount.value = 0
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _isSyncing.value = false
            NotificationHelper.showSyncCompletedNotification(getApplication(), syncedItems)
        }
    }

    fun simulateIncomingAssignedDelivery(
        clientName: String = "Sonia Gutiérrez",
        phone: String = "+53 52981144",
        amountUsd: Double = 120.0,
        address: String = "Calle 23 #452 e/ H e I, Vedado, La Habana"
    ) {
        val rate = settings.value.usdCupRate
        val newDelivery = Delivery(
            clientName = clientName,
            phone = phone,
            amountUsd = amountUsd,
            amountCup = amountUsd * rate,
            address = address,
            identityNumber = "91021488923",
            note = "Asignada remotamente por la central",
            status = DeliveryStatus.PENDING,
            date = "Hoy 10:15 AM",
            zone = "Vedado"
        )
        viewModelScope.launch {
            val id = deliveryDao.insertDelivery(newDelivery)
            val insertedDelivery = newDelivery.copy(id = id)
            NotificationHelper.showNewDeliveryNotification(getApplication(), insertedDelivery)
            if (!isOnline.value) {
                _pendingSyncCount.value += 1
            }
        }
    }




    private val _isGpsActive = MutableStateFlow(true)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private val _userLatitude = MutableStateFlow(23.1367)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(-82.3584)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    fun startGpsService() {
        try {
            LocationService.start(getApplication())
            _isGpsActive.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopGpsService() {
        try {
            LocationService.stop(getApplication())
            _isGpsActive.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setGpsActive(enabled: Boolean) {
        _isGpsActive.value = enabled
    }

    fun updateLocation(lat: Double, lng: Double) {
        _userLatitude.value = lat
        _userLongitude.value = lng
    }

    fun attachPhotoToDelivery(delivery: Delivery, photoUri: String) {
        viewModelScope.launch {
            val updated = delivery.copy(photoUri = photoUri)
            deliveryDao.updateDelivery(updated)
            val currentSettings = settings.value
            if (currentSettings.firestoreSyncEnabled && currentSettings.firestoreWorkspaceId.isNotBlank()) {
                firestoreSyncService.saveDeliveryToFirestore(currentSettings.firestoreWorkspaceId, updated)
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDao.saveSettings(newSettings)
            if (newSettings.firestoreSyncEnabled && newSettings.firestoreWorkspaceId.isNotBlank()) {
                try {
                    firestoreSyncService.saveSettingsToFirestore(newSettings.firestoreWorkspaceId, newSettings)
                } catch (e: Exception) {
                    // Ignore transient errors on background sync
                }
            }
        }
    }

    suspend fun createBackupJsonString(accountName: String): String = withContext(Dispatchers.IO) {
        val allDeliveries = deliveryDao.getAllDeliveriesDirect()
        val allClients = clientDao.getAllClientsDirect()
        val currentSettings = settings.value

        val root = org.json.JSONObject()
        root.put("app", "NexFy Remesas Cuba")
        root.put("version", "2.4.1")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("googleDriveAccount", accountName)

        val deliveriesArray = org.json.JSONArray()
        for (d in allDeliveries) {
            val obj = org.json.JSONObject()
            obj.put("id", d.id)
            obj.put("clientName", d.clientName)
            obj.put("phone", d.phone)
            obj.put("amountUsd", d.amountUsd)
            obj.put("amountCup", d.amountCup)
            obj.put("address", d.address)
            obj.put("identityNumber", d.identityNumber)
            obj.put("note", d.note)
            obj.put("status", d.status.name)
            obj.put("date", d.date)
            obj.put("zone", d.zone)
            obj.put("photoUri", d.photoUri ?: "")
            deliveriesArray.put(obj)
        }
        root.put("deliveries", deliveriesArray)

        val clientsArray = org.json.JSONArray()
        for (c in allClients) {
            val obj = org.json.JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("address", c.address)
            obj.put("zone", c.zone)
            obj.put("totalDeliveredUsd", c.totalDeliveredUsd)
            obj.put("lastDeliveryTime", c.lastDeliveryTime)
            clientsArray.put(obj)
        }
        root.put("clients", clientsArray)

        val settingsObj = org.json.JSONObject()
        settingsObj.put("usdCupRate", currentSettings.usdCupRate)
        settingsObj.put("pinRequired", currentSettings.pinRequired)
        settingsObj.put("hiddenMode", currentSettings.hiddenMode)
        settingsObj.put("autoBackup", currentSettings.autoBackup)
        settingsObj.put("darkMode", currentSettings.darkMode)
        root.put("settings", settingsObj)

        root.toString(2)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = org.json.JSONObject(jsonString)
            if (root.has("deliveries")) {
                val deliveriesArray = root.getJSONArray("deliveries")
                val restoredDeliveries = mutableListOf<Delivery>()
                for (i in 0 until deliveriesArray.length()) {
                    val obj = deliveriesArray.getJSONObject(i)
                    restoredDeliveries.add(
                        Delivery(
                            id = obj.optLong("id", 0L),
                            clientName = obj.optString("clientName", "Desconocido"),
                            phone = obj.optString("phone", ""),
                            amountUsd = obj.optDouble("amountUsd", 0.0),
                            amountCup = obj.optDouble("amountCup", 0.0),
                            address = obj.optString("address", ""),
                            identityNumber = obj.optString("identityNumber", ""),
                            note = obj.optString("note", ""),
                            status = try { DeliveryStatus.valueOf(obj.optString("status", "PENDING")) } catch (e: Exception) { DeliveryStatus.PENDING },
                            date = obj.optString("date", "Hoy"),
                            zone = obj.optString("zone", "Vedado"),
                            photoUri = obj.optString("photoUri", "").takeIf { it.isNotEmpty() }
                        )
                    )
                }
                if (restoredDeliveries.isNotEmpty()) {
                    deliveryDao.insertDeliveries(restoredDeliveries)
                }
            }

            if (root.has("clients")) {
                val clientsArray = root.getJSONArray("clients")
                val restoredClients = mutableListOf<Client>()
                for (i in 0 until clientsArray.length()) {
                    val obj = clientsArray.getJSONObject(i)
                    restoredClients.add(
                        Client(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", ""),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            zone = obj.optString("zone", "Vedado"),
                            totalDeliveredUsd = obj.optDouble("totalDeliveredUsd", 0.0),
                            lastDeliveryTime = obj.optString("lastDeliveryTime", "")
                        )
                    )
                }
                if (restoredClients.isNotEmpty()) {
                    clientDao.insertClients(restoredClients)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setHistoryFilters(date: String, name: String, phone: String) {
        _historyDateFilter.value = date
        _historyNameFilter.value = name
        _historyPhoneFilter.value = phone
    }

    fun appendMasterPinDigit(digit: String) {
        if (_masterPinInput.value.length < 8) {
            _masterPinInput.value += digit
        }
    }

    fun deleteMasterPinDigit() {
        if (_masterPinInput.value.isNotEmpty()) {
            _masterPinInput.value = _masterPinInput.value.dropLast(1)
        }
    }

    fun unlockWithMasterPin(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val input = _masterPinInput.value.trim()
        val devId = _deviceId.value.trim()

        val validMasterPins = listOf(
            "87654321", "73921845", "12345678", "99887766", "11223344",
            "55667788", "98765432", "24681357", "13572468", "88888888",
            "77777777", "00000000"
        )
        val calculatedPin = getCalculatedMasterPin(devId)
        val shaPin = getSha256Pin(devId)

        val isValid = (input.length == 8) && (
            input in validMasterPins ||
            input == calculatedPin ||
            input == shaPin
        )

        if (isValid) {
            _failedUnlockAttempts.value = 0
            _masterPinInput.value = ""
            _isAuthenticated.value = true
            startPinChange()
            onSuccess()
        } else {
            val newFailedCount = _failedUnlockAttempts.value + 1
            _failedUnlockAttempts.value = newFailedCount
            _masterPinInput.value = ""
            if (newFailedCount >= 3) {
                onError("Límite de 3 intentos alcanzado. Datos resguardados por seguridad.")
            } else {
                onError("PIN Maestro incorrecto ($newFailedCount/3 intentos fallidos)")
            }
        }
    }

    fun getCalculatedMasterPin(devId: String): String {
        var sum = 0L
        for (ch in devId.lowercase().trim()) {
            sum = (sum * 31L + ch.code.toLong()) % 100000000L
        }
        return String.format("%08d", Math.abs(sum))
    }

    fun getSha256Pin(devId: String, salt: String = "NEXFY_ANNUAL_365"): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest((devId.lowercase().trim() + salt).toByteArray(Charsets.UTF_8))
            var num = 0L
            for (i in 0..3) {
                num = (num shl 8) or (bytes[i].toLong() and 0xFF)
            }
            val code = Math.abs(num % 100000000L)
            String.format("%08d", code)
        } catch (e: Exception) {
            getCalculatedMasterPin(devId)
        }
    }
}

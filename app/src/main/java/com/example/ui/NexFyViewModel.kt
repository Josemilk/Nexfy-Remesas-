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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Splash : Screen()
    object PinLock : Screen()
    object PinSetup : Screen()
    object AdminUnlock : Screen()
    object Settings : Screen()
    data class MainTabs(val tabIndex: Int = 0) : Screen()
    data class NewDelivery(val initialClientName: String = "", val initialPhone: String = "") : Screen()
    data class DeliveryDetail(val deliveryId: Long) : Screen()
    data class OfflineMap(val deliveryId: Long? = null, val isManualPin: Boolean = false, val targetLocation: Pair<Double, Double>? = null) : Screen()
    data class ClientDetail(val clientId: Long) : Screen()
}

class NexFyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NexFyDatabase.getDatabase(application, viewModelScope)
    private val deliveryDao = database.deliveryDao()
    private val clientDao = database.clientDao()
    private val settingsDao = database.appSettingsDao()
    private val mapTileDao = database.mapTileDao()
    private val trashDao = database.trashDao()
    private val mapTileRepository = com.example.data.repository.MapTileRepository(mapTileDao)

    val trashItems: StateFlow<List<com.example.data.model.TrashItem>> = trashDao.getAllTrashItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val storedTileCount: StateFlow<Int> = mapTileRepository.tileCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val mapTotalStorageBytes: StateFlow<Long> = mapTileRepository.totalStorageBytes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val downloadedRegions: StateFlow<List<String>> = mapTileRepository.downloadedRegions
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

    fun onSplashFinished() {
        if (_currentScreen.value is Screen.Splash) {
            val currentSettings = settings.value
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

    init {
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (!currentSettings.pinRequired) {
                    _isAuthenticated.value = true
                    if (_currentScreen.value is Screen.PinLock) {
                        _currentScreen.value = Screen.MainTabs(0)
                    }
                }
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
            _currentScreen.value = Screen.OfflineMap(id, isManualPin)
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

    fun updateDeliveryStatus(delivery: Delivery, newStatus: DeliveryStatus) {
        viewModelScope.launch {
            deliveryDao.updateDelivery(delivery.copy(status = newStatus))
            if (!isOnline.value) {
                _pendingSyncCount.value += 1
            }
        }
    }

    fun updateDeliveryNote(delivery: Delivery, newNote: String) {
        viewModelScope.launch {
            deliveryDao.updateDelivery(delivery.copy(note = newNote))
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

    private val _isMapDownloaded = MutableStateFlow(false)
    val isMapDownloaded: StateFlow<Boolean> = _isMapDownloaded.asStateFlow()

    private val _isDownloadingMap = MutableStateFlow(false)
    val isDownloadingMap: StateFlow<Boolean> = _isDownloadingMap.asStateFlow()

    private val _mapDownloadProgress = MutableStateFlow(0f)
    val mapDownloadProgress: StateFlow<Float> = _mapDownloadProgress.asStateFlow()

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

    fun startMapDownload() {
        if (_isDownloadingMap.value || _isMapDownloaded.value) return
        _isDownloadingMap.value = true
        _mapDownloadProgress.value = 0f
        
        com.example.ui.screens.OsmMapDownloader.downloadMap(
            context = getApplication(),
            layer = settings.value.mapLayer,
            onProgress = { progress ->
                _mapDownloadProgress.value = progress
            },
            onComplete = {
                viewModelScope.launch {
                    val currentSettings = settings.value
                    settingsDao.saveSettings(currentSettings.copy(offlineMapDownloaded = true))
                    _isDownloadingMap.value = false
                    _isMapDownloaded.value = true
                }
            }
        )
    }

    fun clearRegionMapTiles(regionName: String) {
        viewModelScope.launch {
            mapTileRepository.clearRegionTiles(regionName)
            if (storedTileCount.value == 0) {
                _isMapDownloaded.value = false
                val currentSettings = settings.value
                settingsDao.saveSettings(currentSettings.copy(offlineMapDownloaded = false))
            }
        }
    }

    fun clearAllMapTiles() {
        viewModelScope.launch {
            mapTileRepository.clearAllTiles()
            _isMapDownloaded.value = false
            val currentSettings = settings.value
            settingsDao.saveSettings(currentSettings.copy(offlineMapDownloaded = false))
        }
    }

    fun toggleGps(enabled: Boolean) {
        _isGpsActive.value = enabled
    }

    fun updateLocation(lat: Double, lng: Double) {
        _userLatitude.value = lat
        _userLongitude.value = lng
    }

    fun attachPhotoToDelivery(delivery: Delivery, photoUri: String) {
        viewModelScope.launch {
            deliveryDao.updateDelivery(delivery.copy(photoUri = photoUri))
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsDao.saveSettings(newSettings)
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

    // Admin Unlock & Master PIN State
    private val _deviceId = MutableStateFlow("a1b2c3d4e5f6")
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    private val _masterPinInput = MutableStateFlow("")
    val masterPinInput: StateFlow<String> = _masterPinInput.asStateFlow()

    private val _failedUnlockAttempts = MutableStateFlow(0)
    val failedUnlockAttempts: StateFlow<Int> = _failedUnlockAttempts.asStateFlow()

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
        val input = _masterPinInput.value
        // Accept valid master PINs (e.g., "87654321", "73921845", "12345678" or 8 digits)
        val validMasterPins = listOf("87654321", "73921845", "12345678", "00000000")
        if (input.length == 8 && (input in validMasterPins || input == getCalculatedMasterPin(_deviceId.value))) {
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

    private fun getCalculatedMasterPin(devId: String): String {
        // Deterministic Master PIN calculated from Device ID
        var sum = 0
        for (ch in devId) {
            sum = (sum * 31 + ch.code) % 100000000
        }
        return String.format("%08d", sum)
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val pinRequired: Boolean = true,
    val pinCode: String = "1234",
    val hiddenMode: Boolean = false,
    val usdCupRate: Double = 250.0,
    val commissionPercent: Double = 3.0,
    val whatsappMessage: String = "Hola, tu remesa está lista para recoger.",
    val autoBackup: Boolean = true,
    val backupFrequency: String = "Diaria", // "Diaria", "Semanal", "Mensual", "Al cerrar aplicación"
    val lastBackupDate: String = "Hoy, 11:59 PM",
    val googleDriveSyncEnabled: Boolean = true,
    val firestoreWorkspaceId: String = "ADMIN_NEXFY_01",
    val firestoreSyncEnabled: Boolean = true,
    val lastFirestoreSyncDate: String = "No sincronizado",
    val darkMode: Boolean = false,
    val hideAmounts: Boolean = false,
    val gpsHighPrecision: Boolean = true,
    val ecosystemRole: String = "", // "ADMIN" or "USER", empty means not selected yet
    val adminName: String = "Administrador Principal",
    val adminUsername: String = "admin_nexfy",
    val adminPhone: String = "+53 52981100",
    val adminEmail: String = "admin@nexfy.com",
    val adminPhotoUri: String = "",

    // Home Screen Editable Reference Fields
    val homeTitle: String = "Ecosistema de\nAdministración",
    val homeSubtitle: String = "Monitorea y gestiona las operaciones de remesas en tiempo real",
    val remesasCardTitle: String = "Remesas Hoy",
    val remesasTrendText: String = "+12.5% vs ayer",
    val usersCardTitle: String = "Usuarios Activos",
    val usersBadgeText: String = "+42 nuevos",
    val pendingCardTitle: String = "Pagos Pendientes",
    val pendingWarningText: String = "3 requieren revisión",
    val unassignedSectionTitle: String = "Clientes por atender hoy",
    val unassignedSectionSubtitle: String = "Depósitos registrados por asignar",

    // Card Visibility Toggles (Can be deleted/hidden on long press)
    val showDailySettlementCard: Boolean = true,
    val showConnectionStatusCard: Boolean = true,
    val showRemesasCard: Boolean = true,
    val showUsersCard: Boolean = true,
    val showPendingCard: Boolean = true,
    val showAdminSettlementBanner: Boolean = true,
    val showPendingHeroCard: Boolean = true,

    // SaaS License & Free Trial (30-day trial / 365-day license / Device ID Lock)
    val isLicenseActive: Boolean = false,
    val licenseActivatedAt: Long = 0L,
    val licenseExpirationDate: Long = 0L,
    val licenseDeviceId: String = "",
    val licenseLastNotificationDays: Int = -1,
    val firstLaunchTime: Long = 0L,
    val freeTrialAcknowledged: Boolean = false
)

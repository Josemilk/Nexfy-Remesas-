package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.data.model.MapTile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.data.model.TrashItem
import kotlinx.coroutines.launch

@Database(
    entities = [Delivery::class, Client::class, AppSettings::class, MapTile::class, TrashItem::class],
    version = 4,
    exportSchema = false
)
abstract class NexFyDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
    abstract fun clientDao(): ClientDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun mapTileDao(): MapTileDao
    abstract fun trashDao(): TrashDao

    companion object {
        @Volatile
        private var INSTANCE: NexFyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NexFyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexFyDatabase::class.java,
                    "nexfy_remesas_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: NexFyDatabase) {
            // Pre-populate initial clients matching the prompt images
            val initialClients = listOf(
                Client(name = "Miguel López", phone = "+53 5 123 4567", zone = "Zona: Norte", totalDeliveredUsd = 120.0, lastDeliveryTime = "15:10"),
                Client(name = "Camila Rojas", phone = "+53 5 234 5678", zone = "Zona: Centro", totalDeliveredUsd = 250.50, lastDeliveryTime = "14:45"),
                Client(name = "Jorge Díaz", phone = "+53 5 345 6789", zone = "Zona: Sur", totalDeliveredUsd = 85.0, lastDeliveryTime = "13:20"),
                Client(name = "Ana María Pérez", phone = "+53 5 234 5678", zone = "Zona: Vedado", address = "Calle 23 #451, Vedado, La Habana, Cuba", totalDeliveredUsd = 150.0, lastDeliveryTime = "10:08"),
                Client(name = "Ana López", phone = "+53 5 456 7890", zone = "Metropolitana Norte", totalDeliveredUsd = 1200.0, lastDeliveryTime = "14/10/2024"),
                Client(name = "Carlos Méndez", phone = "+53 5 567 8901", zone = "Zona Sur", totalDeliveredUsd = 850.0, lastDeliveryTime = "12/10/2024"),
                Client(name = "María Fernández", phone = "+53 5 678 9012", zone = "Zona Este", totalDeliveredUsd = 2100.0, lastDeliveryTime = "10/10/2024"),
                Client(name = "Luis Ramírez", phone = "+53 5 789 0123", zone = "Zona Oeste", totalDeliveredUsd = 600.0, lastDeliveryTime = "09/10/2024")
            )
            db.clientDao().insertClients(initialClients)

            // Pre-populate initial deliveries matching the prompt screenshots
            val initialDeliveries = listOf(
                Delivery(
                    clientName = "Ana María Pérez",
                    phone = "+53 5 234 5678",
                    amountUsd = 150.0,
                    amountCup = 37500.0,
                    address = "Calle 23 #451, Vedado, La Habana, Cuba",
                    identityNumber = "88051212345",
                    note = "Cliente solicita entregar después de las 5pm. Portón azul, tocar tres veces.",
                    status = DeliveryStatus.PENDING,
                    date = "15/10/2024",
                    zone = "Vedado",
                    latitude = 23.1367,
                    longitude = -82.3816
                ),
                Delivery(
                    clientName = "Miguel López",
                    phone = "+53 5 123 4567",
                    amountUsd = 120.0,
                    amountCup = 30000.0,
                    address = "Calle 10 #102, Playa, La Habana",
                    status = DeliveryStatus.PENDING,
                    date = "15/10/2024",
                    zone = "Zona: Norte"
                ),
                Delivery(
                    clientName = "Camila Rojas",
                    phone = "+53 5 234 5678",
                    amountUsd = 250.50,
                    amountCup = 62625.0,
                    address = "Ave 31 #3002, Marianao, La Habana",
                    status = DeliveryStatus.PENDING,
                    date = "15/10/2024",
                    zone = "Zona: Centro"
                ),
                Delivery(
                    clientName = "Ana López",
                    phone = "+53 5 456 7890",
                    amountUsd = 1200.0,
                    amountCup = 300000.0,
                    address = "Calle 5ta Ave #12, Miramar",
                    status = DeliveryStatus.DELIVERED,
                    date = "14/10/2024",
                    zone = "Zona: Metropolitana Norte"
                ),
                Delivery(
                    clientName = "Carlos Méndez",
                    phone = "+53 5 567 8901",
                    amountUsd = 850.0,
                    amountCup = 212500.0,
                    address = "Calle Calzada #501, Vedado",
                    status = DeliveryStatus.PENDING,
                    date = "12/10/2024",
                    zone = "Zona: Zona Sur"
                ),
                Delivery(
                    clientName = "María Fernández",
                    phone = "+53 5 678 9012",
                    amountUsd = 2100.0,
                    amountCup = 525000.0,
                    address = "Calle Linea #202, Vedado",
                    status = DeliveryStatus.DELIVERED,
                    date = "10/10/2024",
                    zone = "Zona: Este"
                ),
                Delivery(
                    clientName = "Luis Ramírez",
                    phone = "+53 5 789 0123",
                    amountUsd = 600.0,
                    amountCup = 150000.0,
                    address = "Calle 100 #450, Marianao",
                    status = DeliveryStatus.PENDING,
                    date = "09/10/2024",
                    zone = "Zona: Oeste"
                )
            )
            initialDeliveries.forEach { db.deliveryDao().insertDelivery(it) }

            // Default App Settings matching screenshot
            db.appSettingsDao().saveSettings(
                AppSettings(
                    pinRequired = true,
                    pinCode = "1234",
                    hiddenMode = false,
                    usdCupRate = 250.0,
                    commissionPercent = 3.0,
                    whatsappMessage = "Hola, tu remesa está lista para recoger",
                    autoBackup = true,
                    darkMode = false,
                    hideAmounts = false,
                    offlineMapDownloaded = true,
                    gpsHighPrecision = false,
                    mapLayer = "TOPOGRAPHIC"
                )
            )
        }
    }
}

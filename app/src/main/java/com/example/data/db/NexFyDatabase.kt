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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.data.model.TrashItem
import com.example.data.model.Worker
import kotlinx.coroutines.launch

@Database(
    entities = [Delivery::class, Client::class, AppSettings::class, TrashItem::class, Worker::class],
    version = 13,
    exportSchema = false
)
abstract class NexFyDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
    abstract fun clientDao(): ClientDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun trashDao(): TrashDao
    abstract fun workerDao(): WorkerDao

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
            // Default App Settings
            db.appSettingsDao().saveSettings(
                AppSettings(
                    pinRequired = false,
                    pinCode = "",
                    hiddenMode = false,
                    usdCupRate = 250.0,
                    commissionPercent = 3.0,
                    whatsappMessage = "Hola, tu remesa está lista.",
                    autoBackup = false,
                    darkMode = false,
                    hideAmounts = false,
                    gpsHighPrecision = false,
                )
            )
        }
    }
}

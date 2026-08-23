package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Delivery

object NotificationHelper {

    private const val CHANNEL_ID = "nexfy_delivery_assignments"
    private const val CHANNEL_NAME = "Nuevas Entregas Asignadas"
    private const val SYNC_CHANNEL_ID = "nexfy_sync_channel"
    private const val SYNC_CHANNEL_NAME = "Sincronización Offline"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deliveryChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones locales para nuevas entregas asignadas al repartidor"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                SYNC_CHANNEL_ID,
                SYNC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de actualización cuando se restablece la conexión a internet"
            }

            manager.createNotificationChannel(deliveryChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    fun showNewDeliveryNotification(context: Context, delivery: Delivery) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            delivery.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val deliveryCode = "NX-${String.format("%04d", delivery.id)}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📦 Nueva Entrega Asignada: $deliveryCode")
            .setContentText("Destinatario: ${delivery.clientName} • \$${delivery.amountUsd} USD (${delivery.address})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Se te ha asignado la entrega #$deliveryCode para ${delivery.clientName}.\nMonto: \$${delivery.amountUsd} USD (${delivery.amountCup} CUP)\nDirección: ${delivery.address}\nTeléfono: ${delivery.phone}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(delivery.id.toInt() + 2000, notification)
    }

    fun showSyncCompletedNotification(context: Context, pendingSyncedCount: Int) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("🌐 Conexión Restablecida - Datos Actualizados")
            .setContentText("Se han sincronizado $pendingSyncedCount procesos y entregas pendientes exitosamente.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(999, notification)
    }
}

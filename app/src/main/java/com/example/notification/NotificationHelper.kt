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
    private const val LICENSE_CHANNEL_ID = "nexfy_license_channel"
    private const val LICENSE_CHANNEL_NAME = "Estado de Licencia y Suscripción"

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

            val licenseChannel = NotificationChannel(
                LICENSE_CHANNEL_ID,
                LICENSE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de recordatorio y vencimiento de la licencia anual"
                enableVibration(true)
            }

            manager.createNotificationChannel(deliveryChannel)
            manager.createNotificationChannel(syncChannel)
            manager.createNotificationChannel(licenseChannel)
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

    fun showLicenseExpiringNotification(context: Context, daysRemaining: Int) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            888,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title: String
        val body: String

        when {
            daysRemaining <= 0 -> {
                title = "🚨 ¡Tu Licencia Anual de NexFy Vence Hoy!"
                body = "Tu suscripción de 365 días ha finalizado. Contacta a soporte para renovar tu clave de activación y seguir utilizando la app sin bloqueos."
            }
            daysRemaining == 2 -> {
                title = "⚠️ Licencia NexFy: Quedan 2 días de vigencia"
                body = "Tu plan anual está próximo a expirar en 48 horas. Renueva tu suscripción para garantizar la continuidad operativa."
            }
            daysRemaining == 5 -> {
                title = "⚠️ Aviso de Licencia: Quedan 5 días"
                body = "La licencia de este dispositivo vencerá en 5 días. Contacta a soporte para tramitar tu renovación anual."
            }
            daysRemaining == 10 -> {
                title = "📅 Recordatorio: 10 días restantes de Licencia"
                body = "Tu plan anual de NexFy Remesas vencerá pronto. Puedes solicitar tu renovación con tiempo."
            }
            daysRemaining == 15 -> {
                title = "📅 Recordatorio: 15 días restantes de Licencia"
                body = "Tu suscripción anual tiene 15 días de vigencia restantes. Contacta a soporte para más detalles."
            }
            daysRemaining <= 30 -> {
                title = "📅 Aviso de Suscripción: 1 Mes Restante (30 días)"
                body = "Te informamos que tu licencia anual vencerá en 30 días. Prepárate para renovar tu plan anual de 365 días."
            }
            else -> {
                title = "ℹ️ Estado de Licencia Anual NexFy"
                body = "Quedan $daysRemaining días de vigencia para este dispositivo."
            }
        }

        val notification = NotificationCompat.Builder(context, LICENSE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(8888, notification)
    }
}

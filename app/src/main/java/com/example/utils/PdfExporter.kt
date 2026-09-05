package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.Client
import com.example.data.model.Delivery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    suspend fun exportClientDeliveriesToPdf(
        context: Context,
        client: Client?,
        deliveries: List<Delivery>,
        title: String = "Reporte de Entregas"
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            var yPosition = 50f
            val xMargin = 50f

            canvas.drawText(title, xMargin, yPosition, titlePaint)
            yPosition += 30f

            if (client != null) {
                canvas.drawText("Cliente: ${client.name}", xMargin, yPosition, headerPaint)
                yPosition += 20f
                canvas.drawText("Teléfono: ${client.phone}  |  Zona: ${client.zone}", xMargin, yPosition, textPaint)
                yPosition += 30f
            }

            canvas.drawText("Total de Entregas: ${deliveries.size}", xMargin, yPosition, headerPaint)
            yPosition += 30f

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            deliveries.forEachIndexed { index, delivery ->
                if (yPosition > pageHeight - 100) {
                    document.finishPage(page)
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                canvas.drawText("${index + 1}. Fecha: ${delivery.date}", xMargin, yPosition, headerPaint)
                yPosition += 15f
                canvas.drawText("Importe: $${String.format(Locale.US, "%.2f", delivery.amountUsd)} USD", xMargin + 10f, yPosition, textPaint)
                yPosition += 15f
                canvas.drawText("CUP: $${String.format(Locale.US, "%.2f", delivery.amountCup)}", xMargin + 10f, yPosition, textPaint)
                yPosition += 15f
                canvas.drawText("Estado: ${delivery.status.name}", xMargin + 10f, yPosition, textPaint)
                yPosition += 20f
                
                if (delivery.note.isNotEmpty()) {
                    canvas.drawText("Nota: ${delivery.note}", xMargin + 10f, yPosition, textPaint)
                    yPosition += 20f
                }
            }

            document.finishPage(page)

            // Save PDF
            val fileDir = File(context.cacheDir, "pdfs")
            if (!fileDir.exists()) fileDir.mkdirs()
            val fileName = "reporte_${System.currentTimeMillis()}.pdf"
            val file = File(fileDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return@withContext shareIntent
        } catch (e: Exception) {
            Log.e("PdfExporter", "Error generating PDF", e)
            null
        }
    }
}

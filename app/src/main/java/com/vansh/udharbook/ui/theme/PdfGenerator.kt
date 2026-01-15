package com.vansh.udharbook.ui.theme

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vansh.udharbook.data.Customer
import com.vansh.udharbook.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

object PdfGenerator {

    fun generateAndShareReport(context: Context, customer: Customer, transactions: List<Transaction>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- 1. HEADER ---
        paint.color = Color.parseColor("#4CAF50") // Udhar Green
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("UdharBook Statement", 20f, 50f, paint)

        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Customer: ${customer.name}", 20f, 90f, paint)
        canvas.drawText("Mobile: ${customer.mobile}", 20f, 110f, paint)

        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $date", 400f, 90f, paint)

        // --- 2. TABLE HEADER ---
        val startY = 160f
        paint.color = Color.LTGRAY
        canvas.drawRect(20f, startY - 20, 575f, startY + 10, paint)

        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("DATE", 30f, startY, paint)
        canvas.drawText("NOTE / DETAILS", 130f, startY, paint)
        canvas.drawText("TYPE", 350f, startY, paint)
        canvas.drawText("AMOUNT", 450f, startY, paint)

        // --- 3. DRAW TRANSACTIONS ---
        var y = startY + 40
        paint.isFakeBoldText = false
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

        for (t in transactions) {
            canvas.drawText(sdf.format(Date(t.timestamp)), 30f, y, paint)

            // Truncate note if too long
            val note = if (t.note.length > 25) t.note.take(25) + "..." else t.note.ifEmpty { "-" }
            canvas.drawText(note, 130f, y, paint)

            canvas.drawText(t.type, 350f, y, paint)
            canvas.drawText("Rs. ${t.amount}", 450f, y, paint)

            y += 30

            // Simple page break prevention (stops drawing if page is full)
            if (y > 800) break
        }

        // --- 4. TOTAL ---
        paint.color = Color.parseColor("#EEEEEE")
        canvas.drawRect(20f, y, 575f, y + 40, paint)
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 16f
        canvas.drawText("NET BALANCE:", 300f, y + 25, paint)

        val color = if (customer.balance >= 0) "#4CAF50" else "#F44336" // Green or Red
        paint.color = Color.parseColor(color)
        canvas.drawText("Rs. ${customer.balance.absoluteValue}", 450f, y + 25, paint)

        pdfDocument.finishPage(page)

        // --- 5. SAVE & SHARE ---
        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Statement_${customer.name}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            sharePdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(Intent.createChooser(intent, "Share Report via"))
    }
}
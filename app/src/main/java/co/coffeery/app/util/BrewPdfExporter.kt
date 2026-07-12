package co.coffeery.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import co.coffeery.app.data.local.BrewLogEntity

object BrewPdfExporter {
    fun export(context: Context, log: BrewLogEntity): Intent {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint().apply { typeface = Typeface.DEFAULT; textSize = 14f }
        var y = 40f

        fun line(text: String, size: Float = 14f, bold: Boolean = false) {
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            canvas.drawText(text, 40f, y, paint)
            y += size + 8f
        }

        line("Coffeery Brew Report", 24f, true)
        y += 8f
        line(log.equipmentName, 20f, true)
        y += 4f
        line("Coffee: ${log.coffeeGrams}g | Water: ${log.waterMl}ml | Ratio: 1:${log.ratioDenominator}")
        line("Grind: ${log.grind} | Temp: ${log.tempCelsius}\u00B0C | Time: ${formatDuration(log.totalDurationSec)}")
        if (log.rating > 0) line("Rating: ${"\u2605".repeat(log.rating)} / 5", 16f)
        if (log.tastingNotes.isNotBlank()) {
            y += 8f
            line("Tasting Notes:", 14f, true)
            line("\"${log.tastingNotes}\"")
        }
        if (log.beanName.isNotBlank()) line("Bean: ${log.beanName}")
        y += 16f
        line("Brewed with Coffeery \u2014 ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(log.timestamp)}", 10f)

        doc.finishPage(page)
        val file = java.io.File(context.cacheDir, "brew_report_${log.id}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun formatDuration(sec: Int) = "${sec / 60}m ${sec % 60}s"
}

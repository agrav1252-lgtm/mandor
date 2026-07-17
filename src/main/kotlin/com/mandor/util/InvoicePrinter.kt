package com.mandor.util

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.mandor.domain.model.Invoice
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterJob
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Utility class to print invoices to physical printers and export them as PDF files.
 */
object InvoicePrinter {

    /**
     * Prints the invoice using Java AWT Printable API.
     * This opens the native OS printer dialog and handles Arabic text shaping natively.
     */
    fun printInvoice(invoice: Invoice): Boolean {
        return try {
            val printerJob = PrinterJob.getPrinterJob()
            printerJob.setJobName("Invoice - ${invoice.id}")

            printerJob.setPrintable(object : Printable {
                override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE

                    val g2d = graphics as Graphics2D
                    g2d.translate(pageFormat.imageableX, pageFormat.imageableY)

                    var y = 40
                    val fontTitle = java.awt.Font("Arial", java.awt.Font.BOLD, 18)
                    val fontHeader = java.awt.Font("Arial", java.awt.Font.BOLD, 12)
                    val fontBody = java.awt.Font("Arial", java.awt.Font.PLAIN, 10)

                    // Draw Header
                    g2d.font = fontTitle
                    g2d.drawString("سنتر مندور لمبيعات الجملة", 150, y)
                    y += 30

                    g2d.font = fontHeader
                    g2d.drawString("فاتورة مبيعات رقم: ${invoice.id}", 50, y)
                    g2d.drawString("التاريخ: ${invoice.date}", 350, y)
                    y += 20

                    g2d.drawString("العميل: ${invoice.clientName}", 50, y)
                    if (invoice.clientId.isNotEmpty()) {
                        g2d.drawString("كود العميل: ${invoice.clientId}", 350, y)
                    }
                    y += 20

                    g2d.drawString("نوع الدفع: ${invoice.paymentType}", 50, y)
                    y += 30

                    // Draw Table Headers
                    g2d.font = fontHeader
                    g2d.drawString("الصنف", 50, y)
                    g2d.drawString("السعر", 280, y)
                    g2d.drawString("الكمية", 360, y)
                    g2d.drawString("الإجمالي", 440, y)
                    y += 10
                    g2d.drawLine(50, y, 520, y)
                    y += 20

                    // Draw Table Body
                    g2d.font = fontBody
                    invoice.items.forEach { item ->
                        g2d.drawString(item.product.name, 50, y)
                        g2d.drawString(String.format("%.2f", item.product.price), 280, y)
                        g2d.drawString(item.quantity.toString(), 360, y)
                        g2d.drawString(String.format("%.2f", item.totalPrice), 440, y)
                        y += 18
                    }

                    y += 10
                    g2d.drawLine(50, y, 520, y)
                    y += 20

                    // Draw Total
                    g2d.font = fontHeader
                    g2d.drawString("الإجمالي الكلي: ${String.format("%.2f", invoice.totalAmount)} ج.م", 330, y)
                    y += 40

                    // Footer
                    g2d.font = fontBody
                    g2d.drawString("شكراً لتعاملكم معنا - سنتر مندور", 180, y)

                    return Printable.PAGE_EXISTS
                }
            })

            if (printerJob.printDialog()) {
                printerJob.print()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Exports the invoice as a PDF file using OpenPDF.
     * Opens a file chooser dialog for the user to choose the save destination.
     */
    fun exportToPdf(invoice: Invoice): String? {
        try {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "حفظ الفاتورة كـ PDF"
            fileChooser.fileFilter = FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf")
            fileChooser.selectedFile = File("invoice_${invoice.id}.pdf")

            val userSelection = fileChooser.showSaveDialog(null)
            if (userSelection != JFileChooser.APPROVE_OPTION) return null

            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".pdf")) {
                file = File(file.absolutePath + ".pdf")
            }

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // Define Arabic Unicode Font
            // Use Arial font which is standard. For Linux/MacOS/Windows, we will load a fallback.
            var baseFont: BaseFont? = null
            val paths = listOf(
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/msttcorefonts/Arial.ttf"
            )
            for (path in paths) {
                if (File(path).exists()) {
                    baseFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
                    break
                }
            }
            
            val fontTitle = if (baseFont != null) Font(baseFont, 18f, Font.BOLD) else Font(Font.HELVETICA, 18f, Font.BOLD)
            val fontHeader = if (baseFont != null) Font(baseFont, 12f, Font.BOLD) else Font(Font.HELVETICA, 12f, Font.BOLD)
            val fontBody = if (baseFont != null) Font(baseFont, 10f, Font.NORMAL) else Font(Font.HELVETICA, 10f, Font.NORMAL)

            // Header Paragraphs
            val titlePara = Paragraph("MANDOR WHOLESALE - سنتر مندور", fontTitle)
            titlePara.alignment = Element.ALIGN_CENTER
            document.add(titlePara)
            document.add(Paragraph("\n"))

            document.add(Paragraph("فاتورة مبيعات رقم: ${invoice.id}", fontHeader))
            document.add(Paragraph("التاريخ: ${invoice.date}", fontBody))
            document.add(Paragraph("العميل: ${invoice.clientName}", fontBody))
            if (invoice.clientId.isNotEmpty()) {
                document.add(Paragraph("كود العميل: ${invoice.clientId}", fontBody))
            }
            document.add(Paragraph("نوع الدفع: ${invoice.paymentType}", fontBody))
            document.add(Paragraph("\n"))

            // Items Table
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 1f, 1f, 1.5f))

            // Headers
            val cell1 = PdfPCell(Paragraph("الصنف (Item)", fontHeader))
            val cell2 = PdfPCell(Paragraph("السعر (Price)", fontHeader))
            val cell3 = PdfPCell(Paragraph("الكمية (Qty)", fontHeader))
            val cell4 = PdfPCell(Paragraph("الإجمالي (Total)", fontHeader))

            listOf(cell1, cell2, cell3, cell4).forEach {
                it.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(it)
            }

            // Items
            invoice.items.forEach { item ->
                table.addCell(PdfPCell(Paragraph(item.product.name, fontBody)))
                table.addCell(PdfPCell(Paragraph(String.format("%.2f", item.product.price), fontBody)).apply { horizontalAlignment = Element.ALIGN_CENTER })
                table.addCell(PdfPCell(Paragraph(item.quantity.toString(), fontBody)).apply { horizontalAlignment = Element.ALIGN_CENTER })
                table.addCell(PdfPCell(Paragraph(String.format("%.2f", item.totalPrice), fontBody)).apply { horizontalAlignment = Element.ALIGN_CENTER })
            }

            document.add(table)
            document.add(Paragraph("\n"))

            // Totals
            val totalPara = Paragraph("الإجمالي الكلي: ${String.format("%.2f", invoice.totalAmount)} ج.م", fontHeader)
            totalPara.alignment = Element.ALIGN_RIGHT
            document.add(totalPara)

            document.add(Paragraph("\n\n"))
            val footerPara = Paragraph("شكراً لتعاملكم معنا - سنتر مندور", fontBody)
            footerPara.alignment = Element.ALIGN_CENTER
            document.add(footerPara)

            document.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

package com.example.ratingapp

import android.content.Context
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xddf.usermodel.chart.*
import org.apache.poi.xddf.usermodel.chart.ChartTypes
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun generarInformeExcel(context: Context): File? {
    val calendar = Calendar.getInstance()
    val año = calendar.get(Calendar.YEAR)
    val mes = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1)
    val nombreArchivo = "feedback_${año}_${mes}.csv"
    val archivoCsv = File(context.filesDir, nombreArchivo)

    if (!archivoCsv.exists()) return null

    val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val hoy = Date()
    val hace7dias = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    val registrosSemanales = mutableListOf<Pair<String, String>>()
    val conteo = mutableMapOf("satisfied" to 0, "neutral" to 0, "unsatisfied" to 0)

    archivoCsv.readLines().forEach { linea ->
        val partes = linea.split(",").map { it.trim() }
        if (partes.size == 2) {
            val fecha = formatoFecha.parse(partes[0])
            val tipo = partes[1].lowercase()
            if (fecha != null && fecha.after(hace7dias)) {
                registrosSemanales.add(Pair(partes[0], tipo))
                conteo[tipo] = conteo.getOrDefault(tipo, 0) + 1
            }
        }
    }

    val libro = XSSFWorkbook()
    val hojaResumen = libro.createSheet("Resumen")
    val hojaDetalle = libro.createSheet("Detalle")

    // === Hoja Resumen ===
    val header = hojaResumen.createRow(0)
    header.createCell(0).setCellValue("Tipo")
    header.createCell(1).setCellValue("Cantidad")

    val tipos = listOf("satisfied", "neutral", "unsatisfied")
    tipos.forEachIndexed { i, tipo ->
        val row = hojaResumen.createRow(i + 1)
        row.createCell(0).setCellValue(tipo)
        row.createCell(1).setCellValue(conteo[tipo]?.toDouble() ?: 0.0)
    }

    // === Hoja Detalle ===
    val headerDetalle = hojaDetalle.createRow(0)
    headerDetalle.createCell(0).setCellValue("Fecha")
    headerDetalle.createCell(1).setCellValue("Respuesta")

    registrosSemanales.forEachIndexed { i, (fecha, tipo) ->
        val row = hojaDetalle.createRow(i + 1)
        row.createCell(0).setCellValue(fecha)
        row.createCell(1).setCellValue(tipo)
    }

    // === Gráfico de barras ===
    val drawing = hojaResumen.createDrawingPatriarch()
    val anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 10, 16)
    val chart = drawing.createChart(anchor)

    // Configurar leyenda
    val legend = chart.orAddLegend
    legend.position = LegendPosition.BOTTOM

    // Configurar ejes
    val bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM)
    val leftAxis = chart.createValueAxis(AxisPosition.LEFT)
    leftAxis.crosses = AxisCrosses.AUTO_ZERO

    // Configurar datos
    val data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis)

    // Crear series de datos
    val xData = XDDFDataSourcesFactory.fromStringCellRange(hojaResumen, CellRangeAddress(1, 3, 0, 0))
    val yData = XDDFDataSourcesFactory.fromNumericCellRange(hojaResumen, CellRangeAddress(1, 3, 1, 1))

    val series = data.addSeries(xData, yData)
    series.setTitle("Satisfacción semanal", null)

    // Configurar el gráfico
    chart.plot(data)

    // === Guardar archivo ===
    val archivoSalida = File(context.filesDir, "informe_semanal.xlsx")
    FileOutputStream(archivoSalida).use { out ->
        libro.write(out)
        libro.close()
    }

    return archivoSalida
}
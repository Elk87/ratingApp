package com.example.ratingapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var tvDesde: TextView
    private lateinit var tvHasta: TextView
    private lateinit var tvTotales: TextView
    private lateinit var barChart: BarChart
    private lateinit var dataContainer: LinearLayout
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tvDesde = findViewById(R.id.tvDesde)
        tvHasta = findViewById(R.id.tvHasta)
        tvTotales = findViewById(R.id.tvTotales)
        barChart = findViewById(R.id.barChart)
        dataContainer = findViewById(R.id.dataContainer)

        tvDesde.setOnClickListener { showDatePickerDialog(tvDesde) }
        tvHasta.setOnClickListener { showDatePickerDialog(tvHasta) }

        findViewById<Button>(R.id.btnFiltrar).setOnClickListener {
            actualizarEstadisticas()
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.btnDescargarInforme).setOnClickListener {
            val archivo = generarInformeExcelEnDescargas(this)
            if (archivo != null && archivo.exists()) {
                Toast.makeText(this, "Informe generado en: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No se pudo generar el informe", Toast.LENGTH_SHORT).show()
            }
        }

        setDefaultDates()
        actualizarEstadisticas()
    }

    private fun setDefaultDates() {
        val cal = Calendar.getInstance()
        tvHasta.text = dateFormat.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        tvDesde.text = dateFormat.format(cal.time)
    }

    private fun showDatePickerDialog(target: TextView) {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            target.text = dateFormat.format(calendar.time)
        }

        val dateParts = target.text.toString().split("-").map { it.toInt() }
        DatePickerDialog(this, listener, dateParts[0], dateParts[1] - 1, dateParts[2]).show()
    }

    private fun actualizarEstadisticas() {
        val desde = dateFormat.parse(tvDesde.text.toString()) ?: return
        val hasta = dateFormat.parse(tvHasta.text.toString()) ?: return

        val respuestas = mutableListOf<Pair<String, String>>()
        val conteo = mutableMapOf("buena" to 0, "normal" to 0, "mala" to 0)

        // Leer todos los archivos que empiezan por feedback_
        filesDir.listFiles()?.filter { it.name.startsWith("feedback_") && it.name.endsWith(".csv") }?.forEach { file ->
            file.readLines().forEach { linea ->
                val partes = linea.split(",")
                if (partes.size == 2) {
                    val fecha = partes[0].trim()
                    val tipo = partes[1].trim().lowercase()
                    try {
                        val fechaParsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fecha)
                        if (fechaParsed != null && !fechaParsed.before(desde) && !fechaParsed.after(hasta)) {
                            respuestas.add(fecha to tipo)
                            conteo[tipo] = conteo.getOrDefault(tipo, 0) + 1
                        }
                    } catch (_: Exception) { }
                }
            }
        }

        mostrarTotales(conteo)
        mostrarGrafico(conteo)
        mostrarDetalles(respuestas)
    }

    private fun mostrarTotales(conteo: Map<String, Int>) {
        val total = conteo.values.sum()
        tvTotales.text = "Total respuestas: $total  😊 ${conteo["buena"] ?: 0}  😐 ${conteo["normal"] ?: 0}  😞 ${conteo["mala"] ?: 0}"
    }

    private fun mostrarGrafico(conteo: Map<String, Int>) {
        val entries = listOf(
            BarEntry(0f, conteo["mala"]?.toFloat() ?: 0f),
            BarEntry(1f, conteo["normal"]?.toFloat() ?: 0f),
            BarEntry(2f, conteo["buena"]?.toFloat() ?: 0f)
        )

        val dataSet = BarDataSet(entries, "Respuestas")
        dataSet.colors = listOf(
            resources.getColor(R.color.rojoSuave, null),
            resources.getColor(R.color.amarilloSuave, null),
            resources.getColor(R.color.verdeSuave, null)
        )

        val data = BarData(dataSet)
        data.barWidth = 0.9f
        data.setValueTextSize(14f)

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("😞", "😐", "😊"))
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.invalidate()
    }

    private fun mostrarDetalles(respuestas: List<Pair<String, String>>) {
        dataContainer.removeAllViews()

        respuestas.sortedByDescending { it.first }.forEach { (fecha, tipo) ->
            val fila = TextView(this)
            fila.text = "$fecha - $tipo"
            fila.setPadding(8, 4, 8, 4)
            dataContainer.addView(fila)
        }
    }
}

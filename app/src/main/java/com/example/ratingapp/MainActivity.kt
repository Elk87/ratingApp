package com.example.ratingapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Evita que la pantalla se apague
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        setupFaceClickListeners()
    }

    // Configura los listeners para las 3 caritas
    private fun setupFaceClickListeners() {
        val feedbackMap = mapOf(
            R.id.face1 to "unsatisfied",
            R.id.face2 to "neutral",
            R.id.face3 to "satisfied"
        )
        feedbackMap.forEach { (id, feedback) ->
            findViewById<ImageView>(id)?.setOnClickListener {
                guardarRespuesta(feedback)

                // Lanzar ThanksActivity con transición de fade
                val intent = Intent(this, ThanksActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    private fun guardarRespuesta(valor: String) {
        val fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val linea = "$fecha, $valor\n"

        // Detectar mes actual
        val calendar = Calendar.getInstance()
        val año = calendar.get(Calendar.YEAR)
        val mes = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1)

        val nombreArchivo = "feedback_${año}_${mes}.csv"

        try {
            openFileOutput(nombreArchivo, MODE_APPEND).use {
                it.write(linea.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun generarInformeSemanal(): File? {
        val calendar = Calendar.getInstance()
        val año = calendar.get(Calendar.YEAR)
        val mes = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1)
        val archivoMensual = "feedback_${año}_${mes}.csv"

        val hoy = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val hace7dias = calendar.time

        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        var total = 0
        var satisfecho = 0
        var neutral = 0
        var insatisfecho = 0

        try {
            val archivo = File(filesDir, archivoMensual)
            val lineas = archivo.readLines()

            for (linea in lineas) {
                val partes = linea.split(",").map { it.trim() }
                if (partes.size == 2) {
                    val fecha = formatoFecha.parse(partes[0])
                    val valor = partes[1]

                    if (fecha != null && fecha.after(hace7dias)) {
                        total++
                        when (valor.lowercase()) {
                            "satisfied" -> satisfecho++
                            "neutral" -> neutral++
                            "unsatisfied" -> insatisfecho++
                        }
                    }
                }
            }

            // Crear gráfico de barras en texto
            fun barra(valor: Int) = "█".repeat(valor.coerceAtMost(50))

            val informe = """
            📊 Informe semanal de satisfacción
            
            Total de respuestas: $total

            😀 Satisfecho: $satisfecho   ${barra(satisfecho)}
            😐 Neutral: $neutral         ${barra(neutral)}
            😞 Insatisfecho: $insatisfecho   ${barra(insatisfecho)}
        """.trimIndent()

            // Guardar informe en archivo
            val archivoInforme = File(filesDir, "informe_semanal.txt")
            archivoInforme.writeText(informe)
            return archivoInforme

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


}

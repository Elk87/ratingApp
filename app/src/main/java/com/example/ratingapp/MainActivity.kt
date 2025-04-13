package com.example.ratingapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Date
import java.util.Locale

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



}

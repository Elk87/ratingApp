package com.example.ratingapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val btnEstadisticas = findViewById<ImageButton>(R.id.btnEstadisticas)
        btnEstadisticas.setOnClickListener {
            mostrarDialogoDeCodigo()
        }



    }

    // Configura los listeners para las 3 caritas
    private fun setupFaceClickListeners() {
        val feedbackMap = mapOf(
            R.id.face1 to "mala",
            R.id.face2 to "normal",
            R.id.face3 to "buena"
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
    private fun mostrarDialogoDeCodigo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acceso restringido")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Introduce el código"
        input.textAlignment = View.TEXT_ALIGNMENT_CENTER

        builder.setView(input)

        builder.setPositiveButton("Entrar") { _, _ ->
            val codigoIngresado = input.text.toString()
            if (codigoIngresado == "1234") { // Cambia aquí el código
                val intent = Intent(this, StatsActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }




}

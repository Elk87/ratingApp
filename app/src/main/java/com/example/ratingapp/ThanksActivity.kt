package com.example.ratingapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class ThanksActivity : AppCompatActivity() {

    private val delayMillis: Long = 3000 // 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thanks)

        // Opción: animar la aparición del mensaje (fade in)
        // findViewById<TextView>(R.id.tvThanksMessage).apply {
        //    alpha = 0f
        //    animate().alpha(1f).setDuration(500).start()
        // }

        // Cierra esta activity después del delay
        Handler(Looper.getMainLooper()).postDelayed({
            finish() // Regresa a MainActivity (que se mantiene en back stack)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, delayMillis)
    }
}

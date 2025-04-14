package com.example.ratingapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.util.Calendar
import java.util.Locale

class MonthlyReportWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        try {
            // Verificar que hoy es el día 1.
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            if (day != 1) {
                Log.d("MonthlyReportWorker", "No es el día 1, trabajo terminado exitosamente sin acciones.")
                return Result.success()
            }

            // Determinar el mes anterior para obtener el CSV correspondiente.
            calendar.add(Calendar.MONTH, -1)
            val año = calendar.get(Calendar.YEAR)
            val mes = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1)
            val csvFileName = "feedback_${año}_${mes}.csv"
            val csvFile = File(applicationContext.filesDir, csvFileName)
            if (!csvFile.exists()) {
                Log.d("MonthlyReportWorker", "No se encontró el archivo CSV: $csvFileName")
                return Result.success()
            }

            // Generar el informe Excel usando la función que ya tienes
            val excelFile = generarInformeExcel(applicationContext)
            if (excelFile == null) {
                Log.e("MonthlyReportWorker", "No se pudo generar el informe Excel.")
                return Result.failure()
            }

            // Configurar los parámetros de email
            // REEMPLAZA estos valores con tus datos reales.
            val senderEmail = "your.email@gmail.com"          // Tu correo de envío
            val senderPassword = "yourPassword"                // Tu contraseña (o contraseña de aplicación)
            val recipientEmail = "cmrivas@asesoriaunicom.es"         // Correo de la encargada
            val subject = "Informe Semanal de Feedback - $año-$mes"
            val body = "Adjunto se encuentra el informe semanal de feedback generado automáticamente."

            // Enviar el email usando GMailSender
            val mailSender = GMailSender(senderEmail, senderPassword)
            val emailOk = mailSender.sendMail(subject, body, senderEmail, recipientEmail, excelFile)

            if (emailOk) {
                Log.d("MonthlyReportWorker", "El correo se envió con éxito.")
                // Borrar el CSV para limpiar los datos del mes anterior
                if (csvFile.exists()) {
                    csvFile.delete()
                    Log.d("MonthlyReportWorker", "Se borró el archivo CSV: $csvFileName")
                }
                return Result.success()
            } else {
                Log.e("MonthlyReportWorker", "Error enviando el correo. Reintentando...")
                return Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}

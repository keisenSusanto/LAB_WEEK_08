package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Step 7: Request Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // ✅ Setup WorkManager
        workManager = WorkManager.getInstance(this)

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        // Chain worker
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // Observasi hasil FirstWorker
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("First process is done")
            }
        })

        // ✅ Step 9: Observasi hasil SecondWorker dan panggil NotificationService
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("Second process is done")
                launchNotificationService()
            }
        })
    }

    // 🔹 Fungsi bantu untuk input data ke worker
    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // 🔹 Tampilkan hasil lewat Toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ✅ Step 8: Fungsi untuk menjalankan NotificationService
    private fun launchNotificationService() {
        // Observe LiveData dari NotificationService
        NotificationService.trackingCompletion.observe(this) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        }

        // Intent untuk start NotificationService
        val serviceIntent = Intent(this, NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }

        // Jalankan foreground service
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    companion object {
        const val EXTRA_ID = "Id"
    }
}

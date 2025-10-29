package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.* // Import semua WorkManager tools
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    // Buat instance WorkManager
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

        // Inisialisasi WorkManager instance
        workManager = WorkManager.getInstance(this)

        // Buat constraints — worker hanya jalan saat ada koneksi internet
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        // 🔹 Request pertama untuk FirstWorker
        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        // 🔹 Request kedua untuk SecondWorker
        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        // 🔹 Jalankan kedua worker secara berurutan (chain)
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // 🔹 Observasi hasil worker pertama
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("First process is done")
            }
        })

        // 🔹 Observasi hasil worker kedua
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("Second process is done")
            }
        })
    }

    // Fungsi bantu buat build input data
    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // Tampilkan hasil sebagai Toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

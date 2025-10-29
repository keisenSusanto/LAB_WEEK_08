package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Ambil input data
        val id = inputData.getString(INPUT_DATA_ID)

        // Simulasi proses (delay 3 detik)
        Thread.sleep(3000L)

        // Bangun output data
        val outputData = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()

        // Kembalikan hasil sukses
        return Result.success(outputData)
    }

    companion object {
        const val INPUT_DATA_ID = "inIdThird"
        const val OUTPUT_DATA_ID = "outIdThird"
    }
}

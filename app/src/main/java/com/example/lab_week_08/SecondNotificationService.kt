package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var serviceHandler: Handler
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val mutableID = MutableLiveData<String>()

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread("SecondServiceThread", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        // Ambil channel id dari Intent (fallback ke "002" jika null)
        val channelId = intent?.getStringExtra(EXTRA_ID) ?: "002"

        // Pastikan ada NotificationChannel hanya di API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Second Notification Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, channelPriority)

            val nm = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }

        // Build notification awal
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ganti kalau mau icon lain
            .setContentTitle("Second Notification")
            .setContentText("Second notification starting...")
            .setOngoing(true)

        // Start foreground (safety: startForeground sebelum lama running tasks)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        // Jalankan countdown di background thread
        serviceHandler.post {
            countDownFromFiveToZero(notificationBuilder)
            notifyCompletion(channelId)
            // stop foreground and stop service
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return returnValue
    }

    private fun countDownFromFiveToZero(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (i in 5 downTo 0) {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                // jika terganggu, keluar loop
                break
            }
            notificationBuilder
                .setContentText("$i seconds remaining on Second Notification")
                .setSilent(true)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_ID = "extra_id_2"
        const val NOTIFICATION_ID = 202
        // expose LiveData supaya MainActivity bisa observe
        private val mutable = MutableLiveData<String>()
        val trackingCompletion: MutableLiveData<String> = mutable
    }
}

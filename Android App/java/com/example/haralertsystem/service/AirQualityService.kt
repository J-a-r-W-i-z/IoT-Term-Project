package com.example.haralertsystem.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.haralertsystem.R
import com.example.haralertsystem.shared.SharedViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


private const val channelIdForAlerts = "ALERTS_CHANNEL"

class AirQualityService : Service() {

    private val client = OkHttpClient()
    private var sharedViewModel: SharedViewModel? = null
    private val binder = AirQualityBinder()
    private val handler = Handler(Looper.getMainLooper())
    private var prevAlert = false
    private var prevScenario = ""
    private val apiCallRunnable = object : Runnable {
        override fun run() {
            fetchDataFromApi()
//            fetchDummyData()
            handler.postDelayed(this, 5000)
        }
    }

    inner class AirQualityBinder : Binder() {
        fun getService(): AirQualityService = this@AirQualityService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun setSharedViewModel(viewModel: SharedViewModel) {
        sharedViewModel = viewModel
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Monitoring Air Quality", "Service Running"))

        handler.post(apiCallRunnable)

        return START_STICKY
    }

    private fun createNotification(title: String, message: String): Notification {
        val channelId = "AIR_QUALITY_MONITOR_CHANNEL"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Air Quality Monitor", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun fetchDataFromApi() {
        val request = Request.Builder()
            .url("http://74.225.193.117:5000/live-data")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AirQualityService", "API call failed: ${e.message}")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        try {
                            val jsonObject = JSONObject(responseData)
                            val liveDataString = jsonObject.getString("live-data")
                            val finalDataString = liveDataString.takeIf { it != "null" } ?: "0,0,0,0,0"
                            val dataList = finalDataString.split(",").map {
                                it.ifEmpty { "0" }
                            }

                            val alert = jsonObject.optBoolean("alert", false)
                            val scenario = jsonObject.getString("scenario")

                            updateDataAndDecideNotifications(dataList, alert, scenario, sharedViewModel ?: return)
                        } catch (e: Exception) {
                            Log.e("AirQualityService", "Error parsing JSON: $responseData", e)
                        }
                    } else {
                        Log.e("AirQualityService", "Response body is null")
                    }
                } else {
                    Log.e("AirQualityService", "API call failed with status code: ${response.code}")
                }
            }
        })
    }

    private fun fetchDummyData() {
        val dataList = List(5) { (600..900).random().toString() }
        Log.d("AirQualityService", "Fetched dummy data: $dataList")

        val alert = List(2) { (0..1).random() }.first() == 1
        val burningOrNot = List(2) { (0..1).random() }.first() == 1
        var scenario = if (burningOrNot) "Burning" else "cooking"
        scenario = if (alert) scenario else ""

        Log.d("Alert", alert.toString())
        Log.d("Scenario", scenario)

        updateDataAndDecideNotifications(dataList, alert, scenario, sharedViewModel ?: return)
    }

    private fun updateDataAndDecideNotifications(dataList: List<String>, alert: Boolean, scenario: String, sharedViewModel: SharedViewModel) {
        sharedViewModel.updateSensorData(dataList)

        if (alert) {
            sharedViewModel.updateAlert(scenario, "Air Quality Alert")
        } else {
            sharedViewModel.updateAlert("", "No Alert")
        }

        if (alert && !prevAlert) {
            determinePriority(scenario)
        } else if (alert && prevScenario != scenario) {
            cancelNotification()
            determinePriority(scenario)
        } else if (!alert) {
            sharedViewModel.updateAlert("", "No Alert")
            cancelNotification()
        }

        prevAlert = alert
        prevScenario = scenario
    }

    private fun determinePriority(scenario: String) {
        if (scenario.lowercase() == "smoke detected") {
            pushHighPriorityNotification(scenario)
        } else {
            sendNotification("Air Quality Alert", scenario)
        }
    }


    private fun cancelNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(channelIdForAlerts)
    }

    private fun updateData(dataList: List<String>, scenario: String, sharedViewModel: SharedViewModel) {
        
        sharedViewModel.updateAlert(scenario, "Air Quality Alert")
    }

    fun pushHighPriorityNotification(scenario: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationPattern =
                longArrayOf(0, 2000, 1000)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel =
                NotificationChannel(channelIdForAlerts, "Alerts", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Channel for high priority notifications with sound and vibration"
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.vibrationPattern = vibrationPattern

//            val soundUri =
//                Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification_sound)
//            channel.setSound(soundUri, audioAttributes)

            notificationManager.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelIdForAlerts)
            .setSmallIcon(R.drawable.particle_icon_24dp)
            .setContentTitle("High Priority Alert")
            .setContentText("Detected activity: $scenario")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOngoing(true)
            .setAutoCancel(true)

        val notification = builder.build()

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    2000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            Log.e("Vibrator", "Device does not have a vibrator")
        }

        notificationManager.notify(1, notification)
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelIdForAlerts, "Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelIdForAlerts)
            .setSmallIcon(R.drawable.particle_icon_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

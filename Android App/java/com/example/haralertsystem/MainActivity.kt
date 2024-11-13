package com.example.haralertsystem

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.haralertsystem.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.annotations.Nullable
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.haralertsystem.service.AirQualityService
import com.example.haralertsystem.shared.SharedViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private var airQualityService: AirQualityService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AirQualityService.AirQualityBinder
            airQualityService = binder.getService()
            isServiceBound = true

            airQualityService?.setSharedViewModel(sharedViewModel)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            airQualityService = null
        }
    }

//    override fun onStart() {
//        super.onStart()
//        sharedViewModel.sensorData.observe(this) { dataList ->
//            val latestValues = sharedViewModel.sensorNames.map { sensorName ->
//                dataList[sensorName]?.lastOrNull() ?: "N/A"
//            }
//            updateData(latestValues)
//        }
//        val intent = Intent(this, AirQualityService::class.java)
//        startService(intent)
//    }

    override fun registerReceiver(
        @Nullable receiver: BroadcastReceiver?,
        filter: IntentFilter?
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= 34 && applicationInfo.targetSdkVersion >= 34) {
            super.registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        Intent(this, AirQualityService::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}

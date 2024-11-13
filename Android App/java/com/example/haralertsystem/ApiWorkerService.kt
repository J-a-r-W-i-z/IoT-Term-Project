package com.example.haralertsystem

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.haralertsystem.shared.SharedViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.example.haralertsystem.ui.alerts.AlertModel

class ApiWorkerService : LifecycleService() {

    private lateinit var sharedViewModel: SharedViewModel
    private val client = OkHttpClient()
    private lateinit var scheduler: ScheduledExecutorService

    override fun onCreate() {
        super.onCreate()

        sharedViewModel = ViewModelProvider(this as ViewModelStoreOwner)[SharedViewModel::class.java]

        // Create a scheduled task to fetch API data every 5 seconds
        scheduler = Executors.newScheduledThreadPool(1)
        Log.d("ApiWorkerService", "Scheduler created")
        scheduler.scheduleWithFixedDelay({
//            fetchDataFromApi()
            fetchDummyData()
        }, 0, 5, TimeUnit.SECONDS)

//        // Observe the LiveData from the ViewModel for updates
//        sharedViewModel.dashboardData.observe(this, Observer { data ->
//            // Handle the updated dashboard data
//        })
    }

    private fun fetchDummyData() {
        val dataList = List(5) { (600..900).random().toString() }
        Log.d("ApiWorkerService", "Fetched data: $dataList")
        postCall(dataList)
    }

    // Fetch data from the API and update ViewModel
    private fun fetchDataFromApi() {
        val request = Request.Builder()
            .url("http://12.12.12.12:1883/live-data")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseData = response.body?.string() ?: return@use
                val dataList = responseData.split(",")

                postCall(dataList)
            }
        }
    }

    private fun postCall(dataList: List<String>) {
//        sharedViewModel.updateDashboardData(dataList)
//        Log.d("ApiWorkerService", "Postcall")
//
//        for ((index, value) in dataList.withIndex()) {
//            val threshold = 600 // Example threshold
//            if (value.toFloat() > threshold) {
//                sharedViewModel.addAlert(AlertModel("Sensor $index", value.toFloat()))
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduler.shutdown() // Shut down the scheduler when the service is destroyed
    }
}

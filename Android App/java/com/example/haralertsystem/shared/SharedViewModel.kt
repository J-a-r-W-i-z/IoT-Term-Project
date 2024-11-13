package com.example.haralertsystem.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.haralertsystem.ui.alerts.AlertModel
import android.util.Log

class SharedViewModel : ViewModel() {

    public val sensorNames = listOf("Flammable Gases", "NO2", "Ethanol", "VOC", "CO", "Other Gas")
    public val sensorThresholds = mapOf(
        "Flammable Gases" to 600f,
        "NO2" to 600f,
        "Ethanol" to 700f,
        "VOC" to 700f,
        "CO" to 800f
    )

    private val _alert = MutableLiveData<AlertModel>()
    val alert: LiveData<AlertModel> = _alert

    private val _sensorData = MutableLiveData<MutableMap<String, MutableList<String>>>()
    val sensorData: LiveData<MutableMap<String, MutableList<String>>> = _sensorData

    init {
        _alert.value = AlertModel("No Alert", "")
        _sensorData.value = sensorNames.associateWith { mutableListOf<String>() }.toMutableMap()
    }

    fun updateAlert(description: String, title : String = "No Alert") {
        _alert.postValue(AlertModel(title, description))
    }

    fun updateSensorData(latestValues: List<String>) {
        _sensorData.value?.let { currentData ->
            for ((index, newValue) in latestValues.withIndex()) {
                val sensorName = sensorNames[index]

                newValue.let {
                    val dataList = currentData[sensorName]?.toMutableList() ?: mutableListOf()
                    if (dataList.size >= 10) {
                        dataList.removeAt(0)
                    }
                    dataList.add(it)
                    currentData[sensorName] = dataList
                }
            }

            _sensorData.postValue(currentData)
        }
    }
}

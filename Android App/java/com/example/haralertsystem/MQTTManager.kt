package com.example.haralertsystem

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
//import org.eclipse.paho.android.service.MqttAndroidClient
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.Ack
import org.eclipse.paho.client.mqttv3.*
import android.widget.Toast

/*
# MQTT broker details
broker = "74.225.193.117"
port = 1883
topic = "sensor/data"
username = "raspi"  # Use if authentication is enabled
password = "raspi@AirQuality@123"  # Use if authentication is enabled
 */

class MQTTManager(private val context: Context, private val activity: FragmentActivity) {
    private lateinit var mqttClient: MqttAndroidClient

    fun mqttConnect(brokeraddr: String, clientuser: String, clientpwd: String, onAuthenticated: () -> Unit) {
        Log.d("MQTTConnect", "connecting")
        val clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient (context, "tcp://$brokeraddr", clientId)
        Log.d("MQTTConnect", "connected")


        val connOptions = MqttConnectOptions().apply {
            userName = clientuser
            password = clientpwd.toCharArray()
        }

        Log.d("MQTTConnect", "real connection")
            mqttClient.connect(connOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Toast.makeText(context, "Connection successful!", Toast.LENGTH_SHORT).show()
                    // Add here code executed in case of successful connection
                    onAuthenticated()
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(context, "Connection failed!", Toast.LENGTH_SHORT).show()
                    // exception print
                    Log.d("MqttManagerFailed", exception.toString())
                    // Add here code executed in case of failed connection
                }
            })
    }

    fun mqttSetReceiveListener(onMessageReceived: (String) -> Unit) {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                // Connection Lost
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                // A message has been received
                val data = String(message.payload, charset("UTF-8"))
                onMessageReceived(data)
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Delivery complete
            }
        })
    }

    fun mqttSubscribe(topic: String, qos: Int) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d("MqttManager", "Subscribed")
                    // Successful subscribe
                }override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Failed subscribe
                    Log.d("MqttManager", "Subscribe Failed")
                }
            })
        } catch (e: MqttException) {
            // Check error
        }
    }

    fun mqttPublish(topic: String, msg: String, qos: Int) {
        try {
            val mqttMessage = MqttMessage(msg.toByteArray(charset("UTF-8")))
            mqttMessage.qos = qos
            mqttMessage.isRetained = false
            // Publish the message
            mqttClient.publish(topic, mqttMessage)
        } catch (e: Exception) {
            // Check exception
        }
    }

    fun mqttDisconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    //Successful disconnection
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //Failed disconnection
                }
            })
        } catch (e: MqttException) {
            // Check exception
        }
    }
}
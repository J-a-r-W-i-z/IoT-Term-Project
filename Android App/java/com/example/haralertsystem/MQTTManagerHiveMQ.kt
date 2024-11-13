package com.example.haralertsystem
//
import android.content.Context
//import android.util.Log
import androidx.fragment.app.FragmentActivity
//import com.hivemq.client.mqtt.MqttClient
//import com.hivemq.client.mqtt.MqttGlobalPublishFilter
//import com.hivemq.client.mqtt.datatypes.MqttQos
//import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
//import java.nio.charset.StandardCharsets
//
class MQTTManagerHiveMQ(private val context: Context, private val activity: FragmentActivity) {
//
//    private lateinit var mqttClient: Mqtt3AsyncClient
//
//    fun mqttConnect(brokeraddr: String, brokerport: Int, clientuser: String, clientpwd: String, onAuthenticated: () -> Unit) {
//        Log.d("MQTTConnect", "connecting")
//
//        mqttClient = MqttClient.builder()
//            .useMqttVersion3()
//            .serverHost(brokeraddr)
//            .serverPort(brokerport)
//            .automaticReconnectWithDefaultConfig()
//            .buildAsync()
//
//        mqttClient.connectWith()
//            .simpleAuth()
//            .username(clientuser)
//            .password(clientpwd.toByteArray(StandardCharsets.UTF_8))
//            .applySimpleAuth()
//            .send()
//            .whenComplete { _, throwable ->
//                if (throwable == null) {
//                    Log.d("MQTTConnect", "connected")
//                    onAuthenticated()
//                } else {
//                    Log.e("MQTTConnect", "connection failed", throwable)
//                }
//            }
//    }
//
//    fun mqttSetReceiveListener(onMessageReceived: (String) -> Unit) {
//        mqttClient.publishes(MqttGlobalPublishFilter.ALL) { publish ->
//            val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
//            onMessageReceived(message)
//        }
//    }
//
//    fun mqttSubscribe(topic: String, qos: Int) {
//        mqttClient.subscribeWith()
//            .topicFilter(topic)
////            .qos(MqttQos.fromCode(qos))
//            .send()
//            .whenComplete { _, throwable ->
//                if (throwable == null) {
//                    Log.d("MqttManager", "Subscribed to $topic")
//                } else {
//                    Log.e("MqttManager", "Subscribe failed", throwable)
//                }
//            }
//    }
//
//    fun mqttPublish(topic: String, msg: String, qos: Int) {
//        mqttClient.publishWith()
//            .topic(topic)
//            .payload(msg.toByteArray(StandardCharsets.UTF_8))
////            .qos(MqttQos.fromCode(qos))
//            .send()
//            .whenComplete { _, throwable ->
//                if (throwable != null) {
//                    Log.e("MqttManager", "Publish failed", throwable)
//                }
//            }
//    }
//
//    fun mqttDisconnect() {
//        mqttClient.disconnect()
//            .whenComplete { _, throwable ->
//                if (throwable == null) {
//                    Log.d("MqttManager", "Disconnected successfully")
//                } else {
//                    Log.e("MqttManager", "Disconnection failed", throwable)
//                }
//            }
//    }
}

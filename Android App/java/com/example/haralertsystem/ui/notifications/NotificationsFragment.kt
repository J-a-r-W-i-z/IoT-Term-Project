package com.example.haralertsystem.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.haralertsystem.R
//import com.example.haralertsystem.MQTTManager as MQTTManager
import androidx.fragment.app.activityViewModels
import com.example.haralertsystem.shared.SharedViewModel


class NotificationsFragment : Fragment() {

//    private lateinit var mqttManager: MQTTManager
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

//        mqttManager = MQTTManager(requireContext(), requireActivity())

        val brokerAddress = root.findViewById<EditText>(R.id.broker_address)
        val username = root.findViewById<EditText>(R.id.username)
        val password = root.findViewById<EditText>(R.id.password)
        val topicNameEditText = root.findViewById<EditText>(R.id.topic_name)
        val authenticateButton = root.findViewById<Button>(R.id.authenticate_btn)

        // Handle Authenticate button click
        authenticateButton.setOnClickListener {
            val broker = brokerAddress.text.toString().split(":")
            val user = username.text.toString()
            val pass = password.text.toString()
            val topic = topicNameEditText.text.toString().trim()

            val brokeraddr = broker[0]
            val brokerport = broker[1].toInt()

//            if (broker.isNotEmpty() && user.isNotEmpty() && pass.isNotEmpty() && topic.isNotEmpty()) {
//                mqttManager.mqttConnect(brokeraddr, brokerport, user, pass) {
//                    mqttManager.mqttSubscribe(topic, 1)
//
//                    sharedViewModel.updateMqttConnectionStatus(true)
//                    mqttManager.mqttSetReceiveListener { message ->
//                        sharedViewModel.updateMqttMessage(message)
//                    }
//                }
//            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Disconnect MQTT when the fragment view is destroyed
//        mqttManager.mqttDisconnect()
    }
}
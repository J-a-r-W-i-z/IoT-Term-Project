from flask import Flask, jsonify
import csv
import paho.mqtt.client as mqtt
import random
import time
import threading
import pandas as pd
import numpy as np
import joblib

model = joblib.load('random_forest_model.joblib')

app = Flask(__name__)

# MQTT broker details
broker = "IP_ADDRESS"
port = 0000 # port number
topic = "sensor/data"
username = "username"  # Use if authentication is enabled
password = "password"  # Use if authentication is enabled

# In-memory storage for live data and mutex for thread safety
live_data = []
data_lock = threading.Lock()

# Function to fetch the latest live data
def fetch_live_data():
    data_point = None
    with data_lock:  # Acquire lock for thread safety
        data_point = live_data[-1] if live_data else None
    return data_point

# Function to fetch analytics data
def fetch_data_analytics():
    with data_lock:  # Acquire lock for thread safety
        # Return last min(10, len(live_data)) data points
        length = min(10, len(live_data))
        analytics_result = live_data[-length:]
    return analytics_result

@app.route('/live-data', methods=['GET'])
def get_live_data():
    # Generate and return the latest live data
    latest_data = fetch_live_data()
    print(latest_data)
    scenario="fresh"
    if latest_data:
        data=f"{latest_data}"
        input_data = [int(x) for x in data.split(',')]
        if any(value is None or value == 0 for value in input_data):
            scenario="Invalid"
        else:
            try:
                #input_data = np.array(input_data).reshape(1, -1)
                #scenario = model.predict(input_data)
                input_df = pd.DataFrame([input_data], columns=['Flammable Gases', 'NO2', 'Ethanol', 'VOC', 'CO'])

                # Make the prediction
                scenario = model.predict(input_df)[0]
            except:
                scenario="Invalid"
    else:
        scenario="Invalid"

    if scenario == "sleep":
        scenario = "Poor Ventilation Detected"
    elif scenario == "perfume":
        scenario = "Aerosol Product Detected"
    elif scenario == "burning":
        scenario = "Smoke Detected"
    elif scenario == "cooking":
        scenario = "Cooking Detected"
    alert = True
    if scenario == "fresh" or scenario == "Invalid":
        alert = False
    print(f"Sent: {scenario}")
    return jsonify({"live-data": latest_data, "alert": alert, "scenario": scenario}), 200

@app.route('/analytics', methods=['GET'])
def get_analytics():
    # Generate and return the analytics result
    analytics_result = fetch_data_analytics()
    return jsonify({"analytics": analytics_result}), 200

# Callback function when a message is received
def on_message(client, userdata, msg):
    data = msg.payload.decode()  # Decode the message
    print(f"{data}")
    latest_data=data
    scenario="fresh"
    try:
        if latest_data:
            data=f"{latest_data}"
            input_data = [int(x) for x in data.split(',')]
            if any(value is None or value == 0 for value in input_data):
                scenario="Invalid"
            else:
                    #input_data = np.array(input_data).reshape(1, -1)
                    #scenario = model.predict(input_data)
                    input_df = pd.DataFrame([input_data], columns=['Flammable Gases', 'NO2', 'Ethanol', 'VOC', 'CO'])

                    # Make the prediction
                    scenario = model.predict(input_df)[0]
        else:
            scenario="Invalid"
    except:
        scenario="Invalid"
        print("Invalid Data Format")
    data=latest_data
    print(f"Detected: {scenario}")
    # Add data to live_data list with lock
    with data_lock:
        try:
            if scenario != "Invalid":
                live_data.append(data)
        except ValueError:
            print(f"Invalid data format: {data}")

    # Save and data to a CSV file
    with open('sleep.csv', mode='a') as file:
        writer = csv.writer(file)
        writer.writerow([data])


if __name__ == '__main__':
    # Set up the MQTT client
    client = mqtt.Client()

    # If authentication is enabled, set username and password
    client.username_pw_set(username, password)

    # Connect to the broker
    client.connect(broker, port)

    # Set the callback function to handle messages
    client.on_message = on_message

    # Subscribe to the topic
    client.subscribe(topic)

    # Start the MQTT client loop in a separate thread
    mqtt_thread = threading.Thread(target=client.loop_forever)
    mqtt_thread.start()

    app.run(host='0.0.0.0', port=5000, debug=False)

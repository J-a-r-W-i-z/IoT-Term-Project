import smbus2
import time
import paho.mqtt.client as mqtt
from spidev import SpiDev

# MQTT broker details
broker = "IP_Address"
port = 0000 # Port Number
topic = "sensor/data"
username = "Username"
password = "Password"

# A class to interface with the MCP3008 Analog-to-Digital Converter (ADC)
class MCP3008:
    def __init__(self, bus = 0, device = 0):
        self.bus, self.device = bus, device
        self.spi = SpiDev()
        self.open()
        self.spi.max_speed_hz = 1000000 # 1MHz

    def open(self):
        self.spi.open(self.bus, self.device)
        self.spi.max_speed_hz = 1000000 # 1MHz

    def read(self, channel = 0):
        adc = self.spi.xfer2([1, (8 + channel) << 4, 0])
        data = ((adc[1] & 3) << 8) + adc[2]
        return data

    def close(self):
        self.spi.close()

# I2C address of the sensor (based on the i2cdetect output)
SENSOR_I2C_ADDRESS = 0x08

# Register addresses for the specific gas sensor data
NO2_DATA_REGISTER = 0x01
ETHANOL_DATA_REGISTER = 0x03
VOC_DATA_REGISTER = 0x05
CO_DATA_REGISTER = 0x07

# Initialize I2C bus
bus = smbus2.SMBus(1)  # Use bus number 1 for Raspberry Pi 3

def calculate_flammable_gas_concentration(voltage, Vcc=3.3, RL=10000, R0=1000, A=1000, B=0.6):
    if voltage == 0:
        return 0
    # Calculate Rs (sensor resistance)
    Rs = ((Vcc - voltage) * RL) / voltage

    # Calculate Rs/R0 ratio
    Rs_R0_ratio = Rs / R0

    # Estimate gas concentration in ppm
    gas_concentration_ppm = A * (Rs_R0_ratio ** -B)

    return round(gas_concentration_ppm)


def read_multi_channel_gas_sensor():
    try:
        # Read 2 bytes of data from the sensor's data register
        no2_data = bus.read_i2c_block_data(SENSOR_I2C_ADDRESS, NO2_DATA_REGISTER, 4)
        ethanol_data = bus.read_i2c_block_data(SENSOR_I2C_ADDRESS, ETHANOL_DATA_REGISTER, 4)
        voc_data = bus.read_i2c_block_data(SENSOR_I2C_ADDRESS, VOC_DATA_REGISTER, 4)
        co_data = bus.read_i2c_block_data(SENSOR_I2C_ADDRESS, CO_DATA_REGISTER, 4)

        # Convert the data to a meaningful value (depends on sensor specifications)
        no2_concentration = (no2_data[1] << 8) + no2_data[0]
        ethanol_concentration = (ethanol_data[1] << 8) + ethanol_data[0]
        voc_concentration = (voc_data[1] << 8) + voc_data[0]
        co_concentration = (co_data[1] << 8) + co_data[0]

        return no2_concentration, ethanol_concentration, voc_concentration, co_concentration

    except Exception as e:
        print(f"Error reading from the gas sensor: {e}")
        return None, None, None, None

# Initialize the MCP3008 ADC
adc = MCP3008()

# Create a client object
client = mqtt.Client()

# Set username and password
client.username_pw_set(username, password)

# Connect to the broker
client.connect(broker, port)

# Main loop to continuously read and publish sensor data
while True:
    value = adc.read( channel = 0 )
    print("Applied voltage: %.2f" % (value / 1023.0 * 3.3) )
    voltage = value / 1023.0 * 3.3
    flammable_gases_concentration = calculate_flammable_gas_concentration(voltage)
    no2_concentration, ethanol_concentration, voc_concentration, co_concentration = read_multi_channel_gas_sensor()
    print(f"Flammable Gas Concentration: {flammable_gases_concentration} ppm")
    print(f"NO2 Concentration: {no2_concentration} ppm")
    print(f"Ethanol Concentration: {ethanol_concentration} ppm")
    print(f"VOC Concentration: {voc_concentration} ppm")
    print(f"CO Concentration: {co_concentration} ppm")
    time.sleep(2)  # Delay between readings

    # Publish this data to the cloud using MQTT
    client.publish(topic, f"{flammable_gases_concentration},{no2_concentration},{ethanol_concentration},{voc_concentration},{co_concentration}")

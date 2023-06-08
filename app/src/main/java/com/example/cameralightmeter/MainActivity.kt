package com.example.cameralightmeter

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var lightValueTextView: TextView

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values[0]
            updateLightValue(lux)
            saveLightValue(lux)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // 在這裡處理精確度變化事件（可選）
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lightValueTextView = findViewById(R.id.light_value_textview)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            lightSensorListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        val lightValue = sharedPreferences.getFloat("light_value", 0.0f)
        updateLightValue(lightValue)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }

    private fun updateLightValue(lightValue: Float) {
        lightValueTextView.text = "光感應器值: $lightValue"
    }

    private fun saveLightValue(lightValue: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("light_value", lightValue)
        editor.apply()
    }
}

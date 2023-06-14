package com.example.cameralightmeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var valueTextView: TextView
    private lateinit var supportStatusTextView: TextView

    private var lightSensorValue: Float = 0f
    private var hasReceivedSensorValue: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        valueTextView = findViewById(R.id.light_value_textview)
        supportStatusTextView = findViewById(R.id.supportStatusTextView)

        if (lightSensor == null) {
            supportStatusTextView.text = "設備不支援光感應器"
        } else {
            supportStatusTextView.text = "支援光感應器"
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            if (!hasReceivedSensorValue) {
                valueTextView.text = "預設數值：999"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 當感應器的準確性變化時觸發的回呼方法
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_LIGHT) {
                lightSensorValue = sensorEvent.values[0]
                hasReceivedSensorValue = true

                valueTextView.text = "光感應器數值：$lightSensorValue"
            }
        }
    }
}




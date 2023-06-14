package com.example.cameralightmeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var textView: TextView

    private var lightSensorValue: Float = 0f
    private var hasReceivedSensorValue: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        textView = findViewById(R.id.light_value_textview)

        if (lightSensor == null) {
            textView.text = "設備不支援光感應器"
        } else {
            textView.text = "支援光感應器"
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            // 檢查是否收到光感應器數值
            if (!hasReceivedSensorValue) {
                // 沒有收到數值，顯示預設數值
                textView.text = "預設數值：999"
            }
        }
    }




    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 當感測器準確度變化時觸發
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_LIGHT) {
                lightSensorValue = sensorEvent.values[0]
                hasReceivedSensorValue = true

                // 更新 TextView 的內容
                textView.text = "光感應器數值：$lightSensorValue"
            }
        }
    }

}



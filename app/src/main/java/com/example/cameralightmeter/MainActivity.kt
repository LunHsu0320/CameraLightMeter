package com.example.cameralightmeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var valueTextView: TextView
    private lateinit var supportStatusTextView: TextView
    private lateinit var isoTextView: TextView
    private lateinit var apertureTextView: TextView
    private lateinit var shutterTextView: TextView
    private lateinit var isoSpinner: Spinner
    private lateinit var apertureSpinner: Spinner
    private lateinit var shutterSpinner: Spinner


    private var lightSensorValue: Float = 0f
    private var hasReceivedSensorValue: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        valueTextView = findViewById(R.id.light_value_textview)
        supportStatusTextView = findViewById(R.id.supportStatusTextView)
        isoTextView = findViewById(R.id.isoTextView)
        apertureTextView = findViewById(R.id.apertureTextView)
        shutterTextView = findViewById(R.id.shutterTextView)
        // 找到相應的 Spinner
        isoSpinner = findViewById(R.id.isoSpinner)
        apertureSpinner = findViewById(R.id.apertureSpinner)
        shutterSpinner = findViewById(R.id.shutterSpinner)

        if (lightSensor == null) {
            supportStatusTextView.text = "設備不支援光感應器"
        } else {
            supportStatusTextView.text = "支援光感應器"
        }

        // 設置 ISO、光圈和快門的數據源
        val isoValues = arrayOf("100", "200", "400", "800", "1600")
        val apertureValues = arrayOf("1.4", "2.0", "2.8", "4.0", "5.6")
        val shutterValues = arrayOf("1/1000", "1/500", "1/250", "1/125", "1/60")

        // 創建 ArrayAdapter 並設置數據源
        val isoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, isoValues)
        isoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        isoSpinner.adapter = isoAdapter

        val apertureAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, apertureValues)
        apertureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        apertureSpinner.adapter = apertureAdapter

        val shutterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shutterValues)
        shutterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shutterSpinner.adapter = shutterAdapter

        // ISO Spinner 選擇監聽器
        isoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedISO = isoValues[position]
                isoTextView.text = "ISO：$selectedISO"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
        }

        // 光圈 Spinner 選擇監聽器
        apertureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedAperture = apertureValues[position]
                apertureTextView.text = "光圈：$selectedAperture"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
        }

        // 快門 Spinner 選擇監聽器
        shutterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedShutter = shutterValues[position]
                shutterTextView.text = "快門：$selectedShutter"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
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

                valueTextView.text = "光感應器數值：$lightSensorValue lux"
//                isoTextView.text = "ISO: $isoValue"
//                apertureTextView.text = "光圈: $apertureValue"
//                shutterTextView.text = "快門: $shutterValue"


            }
        }
    }
}




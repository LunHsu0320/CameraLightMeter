package com.example.cameralightmeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
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
    private lateinit var ev_value_textview: TextView

    private lateinit var isoSpinner: Spinner
    private lateinit var apertureSpinner: Spinner
    private lateinit var shutterSpinner: Spinner

    private var lightSensorDefaultValue: Float = 0f
    private var lightSensorValue: Float = 0f
    private var hasReceivedSensorValue: Boolean = false

    private val defaultConstant = 250


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化相關元件
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        valueTextView = findViewById(R.id.light_value_textview)
        supportStatusTextView = findViewById(R.id.supportStatusTextView)
        isoTextView = findViewById(R.id.isoTextView)
        apertureTextView = findViewById(R.id.apertureTextView)
        shutterTextView = findViewById(R.id.shutterTextView)
        ev_value_textview = findViewById(R.id.ev_value_textview)
        isoSpinner = findViewById(R.id.isoSpinner)
        apertureSpinner = findViewById(R.id.apertureSpinner)
        shutterSpinner = findViewById(R.id.shutterSpinner)

        // 檢查設備是否支援光感應器
        if (lightSensor == null) {
            supportStatusTextView.text = "設備不支援光感應器"
        } else {
            supportStatusTextView.text = "支援光感應器"
        }

        Log.d("光感應器常數", "預設常數值為: $defaultConstant")

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

    // Activity 恢復時註冊光感應器監聽器
    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            if (!hasReceivedSensorValue) {
                lightSensorDefaultValue = 160f
                lightSensorValue = lightSensorDefaultValue
                valueTextView.text = "預設數值：$lightSensorDefaultValue"

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                val exposureValue = Math.log10(lightSensorValue.toDouble() / calibrationConstant.toDouble()) / Math.log10(2.0)

                // 將光度曝光值顯示在 TextView 上
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
            }
        }
    }

    // Activity 暫停時取消光感應器監聽器註冊
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

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                val exposureValue = Math.log10(lightSensorValue.toDouble() / calibrationConstant.toDouble()) / Math.log10(2.0)

                // 將光度曝光值顯示在 TextView 上
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
            }
        }
    }
}

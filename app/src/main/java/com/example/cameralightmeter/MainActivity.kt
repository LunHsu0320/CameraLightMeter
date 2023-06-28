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

    private lateinit var prioritySpinner: Spinner
    private lateinit var selectedPriority: String

    private val exposureValue: Double = 0.0


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
        prioritySpinner = findViewById(R.id.priority_spinner)


        // 檢查設備是否支援光感應器
        if (lightSensor == null) {
            supportStatusTextView.text = "設備不支援光感應器"
        } else {
            supportStatusTextView.text = "支援光感應器"
        }

        Log.d("光感應器常數", "預設常數值為: $defaultConstant")

        // 設置 ISO、光圈和快門的數據源
        val isoValues = arrayOf("100", "200", "400", "800", "1600")
        val apertureValues = arrayOf("1.0", "1.4", "2.0", "2.8", "4.0", "5.6")
        val shutterValues = arrayOf("1/1000", "1/500", "1/250", "1/125", "1/60")

        // 創建 ArrayAdapter 並設置數據源
        val isoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, isoValues)
        isoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        isoSpinner.adapter = isoAdapter

        val apertureAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, apertureValues)
        apertureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        apertureSpinner.adapter = apertureAdapter

        val shutterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shutterValues)
        shutterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shutterSpinner.adapter = shutterAdapter

        // 優先考慮選項
        val priorityValues = arrayOf("ISO", "光圈", "快門速度")
        val priorityAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityValues)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = priorityAdapter

        // 優先考慮 Spinner 選擇監聽器
        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPriority = priorityValues[position]
                updateOtherParameters(selectedPriority)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
        }

        // ISO Spinner 選擇監聽器
        isoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedISO = isoValues[position]
                isoTextView.text = "ISO：$selectedISO"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
        }

        // 光圈 Spinner 選擇監聽器
        apertureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedAperture = apertureValues[position]
                apertureTextView.text = "光圈：$selectedAperture"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不執行任何操作
            }
        }

        // 快門 Spinner 選擇監聽器
        shutterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
                lightSensorDefaultValue = 2.5f
                lightSensorValue = lightSensorDefaultValue
                valueTextView.text = "目前為預設數值：$lightSensorDefaultValue"

                // 讀取選擇的 ISO 值
                val selectedISO = isoSpinner.selectedItem.toString().toDouble()

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                val exposureValue =
                    Math.log(lightSensorValue.toDouble() * selectedISO / calibrationConstant.toDouble()) / Math.log(
                        2.0
                    )

                // 計算 EISO
                val eISO = exposureValue + Math.log10(selectedISO / 100) / Math.log10(2.0)
                // 輸出選擇的 ISO 值到日誌
                Log.d("Selected ISO", selectedISO.toString())
                Log.d("eISO", "eISO為: $eISO")

                // 將光度曝光值顯示在 TextView 上
                ev_value_textview.text = "Ev：$exposureValue"
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

                // 讀取選擇的 ISO 值
                val selectedISO = isoSpinner.selectedItem.toString().toDouble()

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                val exposureValue =
                    Math.log(lightSensorValue.toDouble() * selectedISO / calibrationConstant.toDouble()) / Math.log(
                        2.0
                    )

                // 計算 EISO
                val eISO = exposureValue + Math.log10(selectedISO / 100) / Math.log10(2.0)
                // 輸出選擇的 ISO 值到日誌
                Log.d("Selected ISO", selectedISO.toString())
                Log.d("eISO", "eISO為: $eISO")

                // 將光度曝光值顯示在 TextView 上
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
            }
        }
    }

    private fun updateOtherParameters(selectedPriority: String) {
        // 宣告相關變數
        var exposureValue = 0.0
        var recommendedISO = 0.0
        var recommendedAperture = 0.0
        var recommendedShutter = 0.0
        // 根據優先考慮的選項，執行相應的邏輯處理
        when (selectedPriority) {
            "ISO" -> {
                val selectedISO = isoSpinner.selectedItem.toString().toDouble()
                isoTextView.text = "ISO：$selectedISO"

                // 更新光圈和快門的推薦數值
                recommendedAperture =
                    calculateRecommendedAperture(selectedISO, exposureValue, "ISO")
                recommendedShutter =
                    calculateRecommendedShutter(selectedISO, exposureValue, "ISO")
                apertureTextView.text = "光圈：$recommendedAperture"
                shutterTextView.text = "快門：$recommendedShutter"

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                // 更新光度曝光值
                exposureValue =
                    Math.log(lightSensorValue.toDouble() * recommendedISO / calibrationConstant.toDouble()) / Math.log(
                        2.0
                    )

                // 計算 EISO
                val eISO = exposureValue + Math.log10(selectedISO / 100) / Math.log10(2.0)

                // 更新光度曝光值和 EISO 的顯示
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
                Log.d("eISO", "eISO為: $eISO")
            }

            "光圈" -> {
                val selectedAperture = apertureSpinner.selectedItem.toString().toDouble()
                apertureTextView.text = "光圈：$selectedAperture"

                // 更新 ISO 和快門的推薦數值
                recommendedISO =
                    calculateRecommendedISO(selectedAperture, exposureValue, "光圈")
                recommendedShutter =
                    calculateRecommendedShutter(selectedAperture, exposureValue, "光圈")

                isoTextView.text = "ISO：$recommendedISO"
                shutterTextView.text = "快門：$recommendedShutter"

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                // 更新光度曝光值
                exposureValue =
                    Math.log(lightSensorValue.toDouble() * recommendedISO / calibrationConstant.toDouble()) / Math.log(
                        2.0
                    )

                // 計算 EISO
                val eISO = exposureValue + Math.log10(recommendedISO / 100) / Math.log10(2.0)

                // 更新光度曝光值和 EISO 的顯示
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
                Log.d("eISO", "eISO為: $eISO")
            }

            "快門速度" -> {
                val selectedShutter = shutterSpinner.selectedItem.toString().toDouble()
                shutterTextView.text = "快門：$selectedShutter"

                // 更新 ISO 和光圈的推薦數值
                recommendedISO =
                    calculateRecommendedISO(selectedShutter, exposureValue, "快門速度")
                recommendedAperture =
                    calculateRecommendedAperture(selectedShutter, exposureValue, "快門速度")
                isoTextView.text = "ISO：$recommendedISO"
                apertureTextView.text = "光圈：$recommendedAperture"

                // 計算光度曝光值
                val calibrationConstant = defaultConstant
                // 更新光度曝光值
                exposureValue =
                    Math.log(lightSensorValue.toDouble() * recommendedISO / calibrationConstant.toDouble()) / Math.log(
                        2.0
                    )

                // 計算 EISO
                val eISO = exposureValue + Math.log10(recommendedISO / 100) / Math.log10(2.0)

                // 更新光度曝光值和 EISO 的顯示
                ev_value_textview.text = "光度曝光值Ev：$exposureValue"
                Log.d("eISO", "eISO為: $eISO")
            }
        }
    }

    private fun calculateRecommendedAperture(iso: Double, ev: Double, priority: String): Double {
        val c = defaultConstant // 校準常數

        return when (priority) {
            "ISO" -> Math.sqrt(Math.pow(2.0, ev) * c / iso) // 根據 EV 值和選擇的 ISO 計算光圈值
            "光圈" -> 2.8 // 優先考慮光圈，保持基礎光圈值不變
            "快門速度" -> Math.sqrt(
                Math.pow(2.0, ev) * c / (iso * calculateRecommendedShutter(
                    iso,
                    ev,
                    priority
                ))
            ) // 根據 EV 值、ISO 和快門速度計算光圈值
            else -> 2.8
        }
    }

    private fun calculateRecommendedShutter(iso: Double, ev: Double, priority: String): Double {
        val c = defaultConstant // 校準常數

        return when (priority) {
            "ISO" -> Math.pow(2.0, ev) * c / (iso * calculateRecommendedAperture(
                iso,
                ev,
                priority
            )) // 根據 EV 值、ISO 和光圈計算快門速度值
            "光圈" -> 1.0 / 125.0 // 優先考慮光圈，保持基礎快門速度值不變
            "快門速度" -> Math.pow(2.0, ev) * c / (iso * calculateRecommendedAperture(
                iso,
                ev,
                priority
            )) // 根據 EV 值、ISO 和光圈計算快門速度值
            else -> 1.0 / 125.0
        }
    }

    private fun calculateRecommendedISO(aperture: Double, ev: Double, priority: String): Double {
        val c = defaultConstant // 校準常數

        return when (priority) {
            "ISO" -> 200.0 // 優先考慮 ISO，保持基礎 ISO 值不變
            "光圈" -> Math.pow(2.0, ev) * c / (aperture * aperture) // 根據 EV 值和選擇的光圈計算 ISO 值
            "快門速度" -> Math.pow(2.0, ev) * c / (calculateRecommendedShutter(
                200.0,
                ev,
                priority
            ) * aperture) // 根據 EV 值、光圈和快門速度計算 ISO 值
            else -> 200.0
        }
    }


}

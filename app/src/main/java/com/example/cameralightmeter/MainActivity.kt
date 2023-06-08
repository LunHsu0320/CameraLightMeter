package com.example.cameralightmeter

import CameraPreview
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.widget.FrameLayout
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager




class MainActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPreview = CameraPreview(this)
        val previewLayout: FrameLayout = findViewById(R.id.camera_preview)
        previewLayout.addView(mPreview)

        val button: Button = findViewById(R.id.button)

        button.setOnClickListener {
            performMetering()
        }
    }
    private fun performMetering() {
        mCamera?.let { camera ->
            val params: Camera.Parameters = camera.parameters
            val exposureCompensation: Int = params.exposureCompensation

            // 在這裡顯示或輸出曝光補償值
            Log.d(TAG, "Exposure Compensation: $exposureCompensation")
        }
    }

    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: " + e.message)
            null
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mCamera = Camera.open()
            mCamera?.let { camera ->
                mPreview?.setCamera(camera)
                camera.startPreview()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: " + e.message)
        }
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    private fun releaseCamera() {
        mCamera?.let { camera ->
            camera.stopPreview()
            camera.release()
            mCamera = null
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}










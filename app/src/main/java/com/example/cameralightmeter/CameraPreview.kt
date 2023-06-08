import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

import java.io.IOException

class CameraPreview(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private val TAG = "CameraPreview"

    private var mHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null

    init {
        mHolder = holder
        mHolder?.addCallback(this)
    }

    fun setCamera(camera: Camera) {
        mCamera = camera
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            mCamera = Camera.open()
            mCamera?.setPreviewDisplay(holder)
            mCamera?.startPreview()
            // 在这里添加你的測光操作逻辑

        } catch (e: IOException) {
            Log.e(TAG, "Error setting camera preview: " + e.message)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 如果需要处理预览的尺寸变化，可以在这里进行调整
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    private fun releaseCamera() {
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }
}



package com.zf.camera.trick

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.gyf.immersionbar.ImmersionBar
import com.zf.camera.trick.base.BaseActivity
import com.zf.camera.trick.callback.PictureBufferCallback
import com.zf.camera.trick.filter.camera.CONTRAST_DEFAULT
import com.zf.camera.trick.filter.camera.CONTRAST_MAX
import com.zf.camera.trick.filter.camera.CameraFilterBase
import com.zf.camera.trick.filter.camera.PIXELATION_DEFAULT
import com.zf.camera.trick.filter.camera.PIXELATION_MAX
import com.zf.camera.trick.record.VideoRecordListener
import com.zf.camera.trick.ui.CameraGLSurfaceView
import com.zf.camera.trick.ui.CaptureButton
import com.zf.camera.trick.utils.ImageUtils
import com.zf.camera.trick.utils.TrickLog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CameraGLSurfaceViewActivity: BaseActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    private val TAG = "CameraActivity"

    companion object {
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, CameraGLSurfaceViewActivity::class.java))
        }

        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override var isDarkFont = false

    private lateinit var cameraSurfaceView: CameraGLSurfaceView
    private lateinit var mPictureIv: ImageView
    private lateinit var mTimeInfo: AppCompatTextView
    private lateinit var mSeekBar: SeekBar

    private var curType = CameraFilterBase.NO_FILTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_gl_surfaceview)

        initWidget()
        startCamera()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(curType)?.apply { isChecked = true }
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, CameraFilterBase.NO_FILTER, CameraFilterBase.NO_FILTER, "No Filter")
        menu.add(0, CameraFilterBase.FILTER_TYPE_CONTRAST, CameraFilterBase.FILTER_TYPE_CONTRAST, "Contrast")
        menu.add(0, CameraFilterBase.FILTER_TYPE_INVERT, CameraFilterBase.FILTER_TYPE_INVERT, "Invert")
        menu.add(0, CameraFilterBase.FILTER_TYPE_PIXELATION, CameraFilterBase.FILTER_TYPE_PIXELATION, "Pixelation")
        super.onCreateOptionsMenu(menu)
        menu.setGroupCheckable(0, true, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        TrickLog.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        with(item) {
            curType = itemId
            cameraSurfaceView.updateShaderType(itemId)
            if (CameraFilterBase.FILTER_TYPE_CONTRAST == itemId) {
                mSeekBar.progress = ((CONTRAST_DEFAULT / CONTRAST_MAX) * 100).toInt()
            } else if (CameraFilterBase.FILTER_TYPE_PIXELATION == itemId) {
                mSeekBar.progress = ((PIXELATION_DEFAULT / PIXELATION_MAX) * 100).toInt()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        cameraSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            Log.d(TAG, "onWindowFocusChanged: acHeight = ${actionBar?.height}, statusHeight = ${ImmersionBar.getStatusBarHeight(this)}")
            var LP: ViewGroup.LayoutParams = mTimeInfo.layoutParams
            if (LP == null) {
                LP = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val statusBarHeight = if (null != actionBar) actionBar?.height!! else 0
            val topMargin = ImmersionBar.getStatusBarHeight(this) + statusBarHeight
            val MLP = LP as MarginLayoutParams
            MLP.setMargins(MLP.leftMargin, topMargin, MLP.rightMargin, MLP.bottomMargin)
            mTimeInfo.layoutParams = MLP
        }
    }

    private fun initWidget() {
        cameraSurfaceView = findViewById(R.id.cameraView)
        mPictureIv = findViewById(R.id.pictureIv)
        mTimeInfo = findViewById(R.id.time_info)
        mSeekBar = findViewById<SeekBar>(R.id.seek_bar).apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    Log.d(TAG, "onProgressChanged: progress = $progress")
                    cameraSurfaceView.setValue(progress.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
        findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.switch_camera_button).setOnClickListener {
            cameraSurfaceView.switchCamera()
        }
        findViewById<CaptureButton>(R.id.camera_take_button).setClickListener(object : CaptureButton.ClickListener {
            override fun onTakePicture() {
                cameraSurfaceView.takePicture(object : PictureBufferCallback {
                    override fun onPictureToken(data: ByteArray?) {
                        ImageSaveTask(mPictureIv).executeOnExecutor(Executors.newSingleThreadExecutor(), data)
                    }
                })
            }

            override fun onStartRecord() {
                cameraSurfaceView.startRecord(object : VideoRecordListener {
                    private var startTime = 0L
                    private val TIME_START_INFO = "00:00"
                    val timeTask = Runnable {
                        val duration = System.currentTimeMillis() - startTime
                        mTimeInfo.text = String.format("%02d:%02d", duration / 1000 / 60, duration / 1000 % 60)

                        delayedTask()
                    }

                    private fun delayedTask() {
                        mTimeInfo.postDelayed(timeTask, TimeUnit.SECONDS.toMillis(1))
                    }

                    override fun onRecordStart() {
                        Handler(Looper.getMainLooper()).post {
                            TrickLog.d("A-Activity", "onRecordStart")
                            mTimeInfo.alpha = 1.0f
                            startTime = System.currentTimeMillis()
                            mTimeInfo.text = TIME_START_INFO
                            mTimeInfo.postDelayed(timeTask, TimeUnit.SECONDS.toMillis(1))
                        }
                    }

                    override fun onRecordStop() {
                        mTimeInfo.post {
                            mTimeInfo.alpha = 0f
                            Toast.makeText(this@CameraGLSurfaceViewActivity, "录制完成", Toast.LENGTH_SHORT).show()
                            mTimeInfo.text = TIME_START_INFO
                            mTimeInfo.removeCallbacks(timeTask)
                        }
                    }
                })
            }

            override fun onStopRecord() {
                cameraSurfaceView.stopRecord()
            }
        })
    }

    private fun startCamera() {
        if (EasyPermissions.hasPermissions(this,  *permissions)) {
            startPreview()
        } else {
            EasyPermissions.requestPermissions(this, "该权限仅是实现拍摄音视频，请通过权限否则不能使用某些功能！", 100, *permissions)
        }
    }

    private fun startPreview() {
        cameraSurfaceView.openCamera()
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Toast.makeText(this, "您已同意权限申明", Toast.LENGTH_SHORT).show()
    }

    override fun onRationaleDenied(requestCode: Int) {
        Toast.makeText(this, "您已拒绝权限申明", Toast.LENGTH_SHORT).show()
        if (EasyPermissions.somePermissionPermanentlyDenied(this, permissions.toList())) {
            //在权限弹窗中，用户勾选了'不在提示'且拒绝权限的情况触发
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //start preview
        startPreview()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "您已拒绝权限", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    class ImageSaveTask(val mPictureIv: ImageView) : AsyncTask<ByteArray?, Void?, Bitmap>() {
        private var path: String? = null


        override fun onPostExecute(bitmap: Bitmap) {
            mPictureIv.setImageBitmap(bitmap)
            mPictureIv.tag = path
        }

        override fun doInBackground(vararg params: ByteArray?): Bitmap {
            path = ImageUtils.saveImage(params[0])
            return ImageUtils.getCorrectOrientationBitmap(path, Size(mPictureIv.measuredWidth, mPictureIv.measuredHeight))
        }
    }
}
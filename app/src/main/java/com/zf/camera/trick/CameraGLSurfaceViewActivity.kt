package com.zf.camera.trick

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.Size
import android.widget.ImageView
import android.widget.Toast
import com.zf.camera.trick.base.BaseActivity
import com.zf.camera.trick.callback.PictureBufferCallback
import com.zf.camera.trick.ui.CameraGLSurfaceView
import com.zf.camera.trick.ui.CaptureButton
import com.zf.camera.trick.utils.ImageUtils
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.Executors


class CameraGLSurfaceViewActivity: BaseActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    private val TAG = "CameraActivity"

    companion object {
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, CameraGLSurfaceViewActivity::class.java))
        }

        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override var isDarkFont = false

    private var cameraSurfaceView: CameraGLSurfaceView? = null
    private var mPictureIv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_gl_surfaceview)

        initWidget()
        startCamera()
    }


    override fun onResume() {
        super.onResume()
        cameraSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraSurfaceView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initWidget() {
        cameraSurfaceView = findViewById(R.id.cameraView)
        mPictureIv = findViewById(R.id.pictureIv)
        findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.switch_camera_button).setOnClickListener {
            cameraSurfaceView?.switchCamera()
        }
        findViewById<CaptureButton>(R.id.camera_take_button).setClickListener(object : CaptureButton.ClickListener {
            override fun onTakePicture() {
                cameraSurfaceView?.takePicture(object : PictureBufferCallback {
                    override fun onPictureToken(data: ByteArray?) {
                        ImageSaveTask(mPictureIv!!).executeOnExecutor(Executors.newSingleThreadExecutor(), data)
                    }
                })
            }

            override fun onStartRecord() {
                cameraSurfaceView?.startRecord()
            }

            override fun onStopRecord() {
                cameraSurfaceView?.stopRecord()
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
        cameraSurfaceView?.openCamera()
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
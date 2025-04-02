package com.zf.camera.trick

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.zf.camera.trick.base.BaseActivity
import pub.devrel.easypermissions.EasyPermissions


class CameraActivity: BaseActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    companion object {
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, CameraActivity::class.java))
        }

        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override var isDarkFont = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        startCamera()
    }

    private fun startCamera() {
        if (EasyPermissions.hasPermissions(this,  *permissions)) {
            startPreview()
        } else {
            EasyPermissions.requestPermissions(this, "该权限仅是实现拍摄音视频，请通过权限否则不能使用某些功能！", 100, *permissions)
        }
    }

    private fun startPreview() {

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
}
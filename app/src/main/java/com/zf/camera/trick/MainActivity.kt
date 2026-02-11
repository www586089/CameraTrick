package com.zf.camera.trick

import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import com.zf.camera.trick.base.BaseActivity

class MainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<AppCompatTextView>(R.id.camera).setOnClickListener {
            CameraActivity.startActivity(this)
        }
        findViewById<AppCompatTextView>(R.id.camera_gl_surface).setOnClickListener {
            CameraGLSurfaceViewActivity.startActivity(this)
        }
        findViewById<AppCompatTextView>(R.id.picture_tv).setOnClickListener {
            GLTestActivity.start(this)
        }
    }
}
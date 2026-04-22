package com.zf.camera.trick

import android.app.Application
import android.content.Context
import android.util.Log
import com.tencent.bugly.crashreport.CrashReport
import com.zf.camera.trick.utils.ImageUtils

class App : Application() {
    override fun attachBaseContext(base: Context) {
        INSTANCE = this
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: isRelease = ${BuildConfig.isRelease}")
        CrashReport.initCrashReport(applicationContext, "c5f216e93e", !BuildConfig.isRelease);
        ImageUtils.init(this)
    }

    companion object {
        const val TAG = "App"
        private var INSTANCE: App? = null
        fun get(): Application {
            return INSTANCE!!
        }
    }
}

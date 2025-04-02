package com.zf.camera.trick.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.gyf.immersionbar.ImmersionBar

open class BaseActivity : ComponentActivity() {

    protected open var isDarkFont: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .transparentStatusBar()               //透明状态栏，不写默认透明色
            .statusBarDarkFont(isDarkFont)        //状态栏字体是深色，不写默认为亮色
            .transparentNavigationBar()           //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
            .init()
        super.onCreate(savedInstanceState)
    }
}
package com.zf.camera.trick.game.hua.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import com.zf.camera.trick.R
import com.zf.camera.trick.base.BaseActivity

class SettingsActivity : BaseActivity() {

    private lateinit var sp: SharedPreferences
    private lateinit var switchVibrate: Switch
    private lateinit var switchAnim: Switch

    // 配置Key
    private val KEY_VIBRATE = "key_vibrate"
    private val KEY_ANIM = "key_anim"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.title = "设置"

        sp = getSharedPreferences("app_config", MODE_PRIVATE)
        initView()
        initSwitchState()
        initSwitchListener()
    }

    private fun initView() {
        switchVibrate = findViewById(R.id.switch_vibrate)
        switchAnim = findViewById(R.id.switch_anim)
    }

    // 读取本地配置，回显开关
    private fun initSwitchState() {
        switchVibrate.isChecked = sp.getBoolean(KEY_VIBRATE, true)
        switchAnim.isChecked = sp.getBoolean(KEY_ANIM, true)
    }

    // 开关监听 + 保存配置
    private fun initSwitchListener() {
        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_VIBRATE, isChecked).apply()
        }

        switchAnim.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_ANIM, isChecked).apply()
        }
    }

    // 提供静态方法，全局读取配置
    companion object {

        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }

        fun isVibrateEnable(sp: SharedPreferences): Boolean {
            return sp.getBoolean("key_vibrate", true)
        }

        fun isAnimEnable(sp: SharedPreferences): Boolean {
            return sp.getBoolean("key_anim", true)
        }
    }
}
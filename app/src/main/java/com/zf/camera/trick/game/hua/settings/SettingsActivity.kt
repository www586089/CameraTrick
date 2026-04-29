package com.zf.camera.trick.game.hua.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Switch
import com.zf.camera.trick.R
import com.zf.camera.trick.base.BaseActivity
import com.zf.camera.trick.databinding.ActivitySettingsBinding
import com.zf.camera.trick.game.hua.dialog.ColorPickerBottomDialog

class SettingsActivity : BaseActivity() {

    private lateinit var sp: SharedPreferences

    // 配置Key
    private val KEY_VIBRATE = "key_vibrate"
    private val KEY_ANIM = "key_anim"

    val binding: ActivitySettingsBinding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }
    var bgColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        actionBar?.title = "设置"

        sp = getSharedPreferences("app_config", MODE_PRIVATE)
        initView()
        initSwitchState()
        initListener()
    }

    private fun initView() {
        bgColor = getAppColor(sp)
        binding.root.setBackgroundColor(bgColor)
    }

    // 读取本地配置，回显开关
    private fun initSwitchState() {
        binding.switchVibrate.isChecked = sp.getBoolean(KEY_VIBRATE, true)
        binding.switchAnim.isChecked = sp.getBoolean(KEY_ANIM, true)
    }

    // 开关监听 + 保存配置
    private fun initListener() {
        binding.switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_VIBRATE, isChecked).apply()
        }

        binding.switchAnim.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(KEY_ANIM, isChecked).apply()
        }
        binding.selectAppBg.setOnClickListener {
            ColorPickerBottomDialog.show(
                context = this,
                defaultColor = bgColor,
                enableAlpha = true,
                outsideCancel = true
            ) { color, hex ->
                sp.edit().putInt("key_appColor", color).apply()
                binding.root.setBackgroundColor(color)
            }
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

        fun getAppColor(sp: SharedPreferences): Int {
            return sp.getInt("key_appColor", Color.parseColor("#2196F3"));
        }
    }
}
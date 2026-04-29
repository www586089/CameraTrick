package com.zf.camera.trick.game.hua.dialog

import BaseBottomDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar
import com.zf.camera.trick.R

object ColorPickerBottomDialog {

    fun show(
        context: Context,
        defaultColor: Int = 0xFFFFFFFF.toInt(),
        enableAlpha: Boolean = false,
        dim: Float = 0.5f,
        maxHeight: Float = 0.85f,
        outsideCancel: Boolean = true,
        backCancel: Boolean = true,
        onResult: (colorInt: Int, hex: String) -> Unit
    ) {
        val dialog = BaseBottomDialog(context)
            .setLayout(R.layout.layout_color_picker_bottom)
            .setDim(dim)
            .setMaxHeight(maxHeight)
            .setOutsideCancel(outsideCancel)
            .setBackCancel(backCancel)

        val root = dialog.getRootView()
        val colorPicker = root.findViewById<ColorPickerView>(R.id.colorPickerView)
        val alphaSlideBar = root.findViewById<AlphaSlideBar>(R.id.alphaSlideBar)
        val brightnessSlideBar = root.findViewById<BrightnessSlideBar>(R.id.brightnessSlideBar)
        val preView = root.findViewById<View>(R.id.view_preview)
        val tvHex = root.findViewById<TextView>(R.id.tv_hex_code)
        val tvCancel = root.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm = root.findViewById<TextView>(R.id.tv_confirm)

        var currentColor = defaultColor
        colorPicker.apply {
            setInitialColor(defaultColor)
            attachAlphaSlider(alphaSlideBar)
            attachBrightnessSlider(brightnessSlideBar)
        }


        // ====================== 修复完毕 ======================
        fun refreshUI(color: Int) {
            currentColor = color
            preView.setBackgroundColor(color)
            tvHex.text = if (enableAlpha) {
                // 8位 ARGB
                java.lang.String.format("#%08X", color)
            } else {
                // 6位 RGB
                java.lang.String.format("#%06X", color and 0xFFFFFF)
            }
        }

        refreshUI(defaultColor)

        colorPicker.setColorListener(object : ColorListener {
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                refreshUI(color)
            }
        })

        tvCancel.setOnClickListener { dialog.dismiss() }
        tvConfirm.setOnClickListener {
            onResult(currentColor, tvHex.text.toString())
            dialog.dismiss()
        }

        dialog.show()
    }
}
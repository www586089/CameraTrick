package com.zf.camera.trick.bean

import com.zf.camera.trick.filter.CameraFilterFactory

object MenuInstance {
    private val menuList = listOf(
        MenuBean(0, CameraFilterFactory.NO_FILTER, CameraFilterFactory.NO_FILTER, "No Filter"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_CONTRAST, CameraFilterFactory.FILTER_TYPE_CONTRAST, "Contrast"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_INVERT, CameraFilterFactory.FILTER_TYPE_INVERT, "Invert"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_PIXELATION, CameraFilterFactory.FILTER_TYPE_PIXELATION, "Pixelation"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_HUE, CameraFilterFactory.FILTER_TYPE_HUE, "Hue"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_GAMMA, CameraFilterFactory.FILTER_TYPE_GAMMA, "Gamma"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_BRIGHTNESS, CameraFilterFactory.FILTER_TYPE_BRIGHTNESS, "Brightness"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_SEPIA_TONE, CameraFilterFactory.FILTER_TYPE_SEPIA_TONE, "SepiaTone"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_GRAY_SCALE, CameraFilterFactory.FILTER_TYPE_GRAY_SCALE, "GrayScale"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_SHARPNESS, CameraFilterFactory.FILTER_TYPE_SHARPNESS, "Sharpness"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_SOBEL_EDGE_DETECTION, CameraFilterFactory.FILTER_TYPE_SOBEL_EDGE_DETECTION, "Sobel Edge Detection"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_THRESHOLD_EDGE_DETECTION, CameraFilterFactory.FILTER_TYPE_THRESHOLD_EDGE_DETECTION, "Threshold Edge Detection"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_THREE_X_THREE_CONVOLUTION, CameraFilterFactory.FILTER_TYPE_THREE_X_THREE_CONVOLUTION, "3x3 Convolution"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_EMBOSS, CameraFilterFactory.FILTER_TYPE_EMBOSS, "Emboss"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_POSTERIZE, CameraFilterFactory.FILTER_TYPE_POSTERIZE, "Posterize"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_FILTER_GROUP, CameraFilterFactory.FILTER_TYPE_FILTER_GROUP, "Grouped filters"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_SATURATION, CameraFilterFactory.FILTER_TYPE_SATURATION, "Saturation"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_EXPOSURE, CameraFilterFactory.FILTER_TYPE_EXPOSURE, "Exposure"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_HIGHLIGHT_SHADOW, CameraFilterFactory.FILTER_TYPE_HIGHLIGHT_SHADOW, "Highlight Shadow"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_MONOCHROME, CameraFilterFactory.FILTER_TYPE_MONOCHROME, "Monochrome"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_OPACITY, CameraFilterFactory.FILTER_TYPE_OPACITY, "Opacity"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_RGB, CameraFilterFactory.FILTER_TYPE_RGB, "RGB"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_WHITE_BALANCE, CameraFilterFactory.FILTER_TYPE_WHITE_BALANCE, "White Balance"),
        MenuBean(0, CameraFilterFactory.FILTER_TYPE_VIGNETTE, CameraFilterFactory.FILTER_TYPE_VIGNETTE, "Vignette"),
    )

    fun getMenuList(): List<MenuBean> {
        return menuList
    }
}
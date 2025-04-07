package com.zf.camera.trick.utils

import android.hardware.Camera

open class CameraSizeComparator(private val isAsc: Boolean) :
    Comparator<Camera.Size> {
    override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
        return if (lhs.width * lhs.height == rhs.width * rhs.height) {
            0
        } else {
            if (isAsc) {
                return if (lhs.width * lhs.height > rhs.width * rhs.height) 1 else -1
            }
            if (lhs.width * lhs.height > rhs.width * rhs.height) -1 else 1
        }
    }
}

package com.zf.camera.trick.base

import com.zf.camera.trick.filter.sample.IShape

interface IAction {
    // 获取动作
    fun getAction(): IShape
    fun getName(): String
}
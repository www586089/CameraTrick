package com.zf.camera.trick.filter.sample

import android.content.Context
import android.view.Menu
import com.zf.camera.trick.base.IAction

interface IShape {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onSurfaceDestroyed()
    fun drawFrame()
}

const val SHAPE_TYPE_NONE = 0
const val SHAPE_TYPE_TRIANGLE_NORMAL = SHAPE_TYPE_NONE + 1
const val SHAPE_TYPE_TRIANGLE_VBO = SHAPE_TYPE_NONE + 2
const val SHAPE_TYPE_TRIANGLE_VAO = SHAPE_TYPE_NONE + 3
const val SHAPE_TYPE_TRIANGLE_EBO = SHAPE_TYPE_NONE + 4


fun getShapeAction(ctx: Context, actions: MutableMap<Int, IAction>) {
    //OpenGL ES 基础
    actions[SHAPE_TYPE_TRIANGLE_NORMAL] = object : IAction {
        override fun getAction(): IShape {
            return NormalTriangle(ctx)
        }

        override fun getName(): String {
            return "三角形基本绘制"
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_VBO] = object : IAction {
        override fun getAction(): IShape {
            return VBOTriangle(ctx)
        }

        override fun getName(): String {
            return "VBO"
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_VAO] = object : IAction {
        override fun getAction(): IShape {
            return VAOTriangle(ctx)
        }

        override fun getName(): String {
            return "VAO"
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_EBO] = object : IAction {
        override fun getAction(): IShape {
            return EBOTriangle(ctx)
        }

        override fun getName(): String {
            return "EBO"
        }
    }
}


fun onCreateOptionsMenu(menu: Menu) {

    //添加一个子菜单
    val baseGroupId = SHAPE_TYPE_NONE
    val baseOpenGL = menu.addSubMenu(baseGroupId, baseGroupId, baseGroupId, "OpenGL ES基础")
    baseOpenGL.add(baseGroupId, SHAPE_TYPE_TRIANGLE_NORMAL, SHAPE_TYPE_TRIANGLE_NORMAL, "基本绘制")
    baseOpenGL.add(baseGroupId, SHAPE_TYPE_TRIANGLE_VBO, SHAPE_TYPE_TRIANGLE_VBO, "VB0")
    baseOpenGL.add(baseGroupId, SHAPE_TYPE_TRIANGLE_VAO, SHAPE_TYPE_TRIANGLE_VAO, "VAO")
    baseOpenGL.add(baseGroupId, SHAPE_TYPE_TRIANGLE_EBO, SHAPE_TYPE_TRIANGLE_EBO, "EBO")
    baseOpenGL.setGroupCheckable(baseGroupId, true, true)

    val testGroupId = 100
    val subMenuTest = menu.addSubMenu(testGroupId, 1, 1, "测试菜单")
    subMenuTest.add(testGroupId, 100, 100, "测试1")
    subMenuTest.add(testGroupId, 101, 101, "测试2")
    subMenuTest.setGroupCheckable(testGroupId, true, true)
}

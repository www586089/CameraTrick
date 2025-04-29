package com.zf.camera.trick.filter.sample

import android.content.Context
import android.view.Menu
import com.zf.camera.trick.base.IAction
import com.zf.camera.trick.base.IGroup

interface IShape {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onSurfaceDestroyed()
    fun drawFrame()
}

const val SHAPE_TYPE_NONE = 0

const val GP_BASE_OPEN_GL = SHAPE_TYPE_NONE             //OpenGL ES 基础
const val GP_GPU_IMAGE_FILTER = SHAPE_TYPE_NONE + 100   //GPU 图像滤镜
const val GP_TEST = SHAPE_TYPE_NONE + 1000              //测试菜单

//OpenGL ES 基础 begin
const val SHAPE_TYPE_TRIANGLE_NORMAL = SHAPE_TYPE_NONE + 1
const val SHAPE_TYPE_TRIANGLE_VBO = SHAPE_TYPE_NONE + 2
const val SHAPE_TYPE_TRIANGLE_VAO = SHAPE_TYPE_NONE + 3
const val SHAPE_TYPE_TRIANGLE_EBO = SHAPE_TYPE_NONE + 4
const val SHAPE_TYPE_QUAD_TEXTURE = SHAPE_TYPE_NONE + 5             //使用纹理绘制四边形
const val SHAPE_TYPE_QUAD_TRANSFORM = SHAPE_TYPE_NONE + 6           //使用纹理绘制四边形
// -->end


//GpuImage 开源项目图像滤镜 begin
const val GPU_IMAGE_FILTER_INVERT = GP_GPU_IMAGE_FILTER + 1         //图像反转

//-->end

const val SHAPE_TEST1 = GP_TEST + 1
const val SHAPE_TEST2 = GP_TEST + 2



fun getShapeAction(ctx: Context, actions: MutableMap<Int, IAction>, groups: MutableMap<Int, IGroup>) {
    //菜单组设置
    groups.put(GP_BASE_OPEN_GL, object : IGroup {
        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }

        override fun getGroupName(): String {
            return "OpenGL ES基础"
        }
    })

    groups[GP_GPU_IMAGE_FILTER] = object : IGroup {
        override fun getGroupId(): Int {
            return GP_GPU_IMAGE_FILTER
        }

        override fun getGroupName(): String {
            return "GPUImage 图像滤镜"
        }
    }



    groups.put(GP_TEST, object : IGroup {
        override fun getGroupId(): Int {
            return GP_TEST
        }

        override fun getGroupName(): String {
            return "测试菜单"
        }
    })


    //OpenGL ES 基础 =====================================>begin
    actions[SHAPE_TYPE_TRIANGLE_NORMAL] = object : IAction {
        override fun getAction(): IShape {
            return NormalTriangle(ctx)
        }

        override fun getName(): String {
            return "三角形基本绘制"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_VBO] = object : IAction {
        override fun getAction(): IShape {
            return VBOTriangle(ctx)
        }

        override fun getName(): String {
            return "VBO"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_VAO] = object : IAction {
        override fun getAction(): IShape {
            return VAOTriangle(ctx)
        }

        override fun getName(): String {
            return "VAO"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }
    actions[SHAPE_TYPE_TRIANGLE_EBO] = object : IAction {
        override fun getAction(): IShape {
            return EBOTriangle(ctx)
        }

        override fun getName(): String {
            return "EBO"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }

    actions[SHAPE_TYPE_QUAD_TEXTURE] = object : IAction {
        override fun getAction(): IShape {
            return TextureQuad(ctx)
        }

        override fun getName(): String {
            return "纹理的使用"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }

    actions[SHAPE_TYPE_QUAD_TRANSFORM] = object : IAction {
        override fun getAction(): IShape {
            return TextureQuadTransform(ctx)
        }

        override fun getName(): String {
            return "坐标变换"
        }

        override fun getGroupId(): Int {
            return GP_BASE_OPEN_GL
        }
    }

    //OpenGL ES 基础 =====================================>>end


    //GpuImage 开源项目图像滤镜 =====================================>begin
    actions[GPU_IMAGE_FILTER_INVERT] = object : IAction {
        override fun getAction(): IShape {
            return GIColorInvertFilter(ctx)
        }

        override fun getName(): String {
            return "颜色反转（Invert）"
        }

        override fun getGroupId(): Int {
            return GP_GPU_IMAGE_FILTER
        }
    }
    //GpuImage 开源项目图像滤镜 <===================================== end

    actions[SHAPE_TEST1] = object : IAction {
        override fun getAction(): IShape {
            return TextureQuad(ctx)
        }

        override fun getName(): String {
            return "测试1"
        }

        override fun getGroupId(): Int {
            return GP_TEST
        }
    }
    actions[SHAPE_TEST2] = object : IAction {
        override fun getAction(): IShape {
            return TextureQuad(ctx)
        }

        override fun getName(): String {
            return "测试2"
        }

        override fun getGroupId(): Int {
            return GP_TEST
        }
    }
}


fun onCreateOptionsMenu(menu: Menu, actions: MutableMap<Int, IAction>, groups: MutableMap<Int, IGroup>) {
    groups.forEach { menuGroup ->
        val groupId = menuGroup.key
        val groupName = menuGroup.value.getGroupName()

        //创建子菜单
        val subMenu = menu.addSubMenu(groupId, groupId, groupId, groupName)
        actions.forEach { menuItem ->
            val menuItemId = menuItem.key
            val menuItemName = menuItem.value.getName()
            if (groupId == menuItem.value.getGroupId()) {//创建子菜单项
                subMenu.add(groupId, menuItemId, menuItemId, menuItemName)
            }
        }
        subMenu.setGroupCheckable(groupId, true, true)
    }
}

package com.zf.camera.trick.filter;


import android.opengl.GLES20;
import android.opengl.Matrix;

import com.zf.camera.trick.gl.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    // 顶点着色器代码
    private final String vertexShaderCode =
                    // 传入变换矩阵
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // 变换矩阵与顶点坐标相乘等到新的坐标
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";


    // 片段着色器代码
    private final String fragmentShaderCode =
                    "precision mediump float;\n" +
                    "uniform vec4 vColor;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = vColor;\n" +
                    "}\n";
    /**
     * Shader程序中矩阵属性的句柄
     */
    private int vPMatrixHandle;


    // vPMatrix是“模型视图投影矩阵”的缩写
    // 最终变化矩阵
    private final float[] mMVPMatrix = new float[16];
    // 投影矩阵
    private final float[] mProjectionMatrix = new float[16];
    // 相机矩阵
    private final float[] mViewMatrix = new float[16];


    // 顶点坐标缓冲区
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    // 此数组中每个顶点的坐标数
    static final int COORDS_PER_VERTEX = 3;
    // 三角形三个点的坐标，逆时针绘制
    static float[] triangleCoords = {   // 坐标逆时针顺序
            -0.5f,  0.5f, 0f,       //left top
            -0.5f, -0.5f, 0f,       //left bottom
             0.5f, -0.5f, 0f,       //right bottom
             0.5f,  0.5f, 0f        //right top
    };

    private short[] indexArray = {
            0, 1, 2,
            0, 2, 3
    };

    // 设置颜色为红色
    private float[] color = {1.0f, 0.0f, 0.0f, 1.0f};

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    /**
     * Shader程序中顶点属性的句柄
     */
    private int positionHandle;
    /**
     * Shader程序中颜色属性的句柄
     */
    private int colorHandle;

    /**
     * OpenGL ES程序句柄
     */
    private int mProgram;

    public Square() {
        // 初始化形状坐标的顶点字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        ByteBuffer indexBB = ByteBuffer.allocateDirect(indexArray.length * 2);
        indexBB.order(ByteOrder.nativeOrder());
        indexBuffer = indexBB.asShortBuffer();
        indexBuffer.put(indexArray);
        indexBuffer.position(0);
    }

    public static void scale(float[] coords, int stride, float sx, float sy, float sz) {
        float[] scaleM = {
                sx, 0, 0,
                0, sy, 0,
                0, 0, sz
        };

        for (int i = 0; i < coords.length; i += stride) {
            float x = coords[i];
            float y = coords[i + 1];
            float z = coords[i + 2];

            coords[i] = scaleM[0] * x;
            coords[i + 1] = scaleM[4] * y;
            coords[i + 2] = scaleM[8] * z;
        }
    }


    public void surfaceCreated() {
        // 加载顶点着色器代码
        int vertexShader = GLESUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        // 加载片段着色器代码
        int fragmentShader = GLESUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器添加到程序中
        GLES20.glAttachShader(mProgram, vertexShader);
        // 将片段着色器添加到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 链接OpenGL ES程序
        GLES20.glLinkProgram(mProgram);

        // 获取顶点着色器vPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 获取片段着色器vColor成员的句柄
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 获取绘制矩阵句柄
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }


    public void surfaceChanged(int width, int height) {
        // 设置OpenGL ES画布大小
        GLES20.glViewport(0, 0, width, height);

        float ratio;
        if (width > height) {
            ratio = (float) width / height;
            // 横屏使用
            // 透视投影，特点：物体离视点越远，呈现出来的越小。离视点越近，呈现出来的越大
            // 该投影矩阵应用于对象坐标
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        } else {
            ratio = (float) height / width;
            // 竖屏使用
            // 透视投影，特点：物体离视点越远，呈现出来的越小。离视点越近，呈现出来的越大
            // 该投影矩阵应用于对象坐标
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
        }

        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 3f,    //摄像机位置
                0f, 0f, 0f,    //观察点（眼睛）位置
                0f, 1.0f, 0.0f);    //摄像机up向量

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }


    public void draw() {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // 重新绘制背景色为黑色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        // 为三角形顶点启用控制柄
        GLES20.glEnableVertexAttribArray(positionHandle);
        // 准备三角坐标数据
        GLES20.glVertexAttribPointer(
                positionHandle,     // 执行要配置的属性句柄（编号）
                COORDS_PER_VERTEX,  // 指定每个顶点属性的分量数
                GLES20.GL_FLOAT,    // 指定每个分量的数据类型
                false,              // 指定是否将数据归一化到 [0,1] 或 [-1,1] 范围内
                vertexStride,       // （步长）指定连续两个顶点属性间的字节数。如果为 0，则表示顶点属性是紧密排列的
                vertexBuffer        // 指向数据缓冲对象
        );
        // 将缩放矩阵传递给着色器程序
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // 画三角形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexArray.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // 禁用顶点阵列
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}


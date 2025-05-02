package com.zf.camera.trick.filter.camera;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.zf.camera.trick.R;
import com.zf.camera.trick.filter.AFilter;
import com.zf.camera.trick.gl.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class CameraFilterBase extends AFilter {


    private String TAG = "CameraFilterBase";

    public static final int NO_FILTER = 0;
    public static final int FILTER_TYPE_CONTRAST = NO_FILTER + 1;
    public static final int FILTER_TYPE_INVERT = NO_FILTER + 2;
    public static final int FILTER_TYPE_PIXELATION = NO_FILTER + 3;
    public static final int FILTER_TYPE_HUE = NO_FILTER + 4;
    public static final int FILTER_TYPE_GAMMA = NO_FILTER + 5;

    protected IAdjuster mAdjuster = null;
    protected IAdjuster createAdjuster() {
        return null;
    }

    public boolean hasAdjuster() {
        return null != mAdjuster;
    }

    public float getDefaultProgress() {
        if (null != mAdjuster) {
            return mAdjuster.getDefaultProgress();
        }

        return 0f;
    }
    public IAdjuster getAdjuster() {
        return mAdjuster;
    }

    public void adjust(float percentage) {
        if (null != mAdjuster) {
            mAdjuster.adjust(percentage);
        }
    }

    public static CameraFilterBase getFilter(Resources resources, int type) {
        switch (type) {
            case NO_FILTER:
                return new CameraFilerNoChange(resources);

            case FILTER_TYPE_CONTRAST:
                return new CameraFilterContrast(resources);

                case FILTER_TYPE_INVERT:
                return new CameraFilterInvert(resources);

                case FILTER_TYPE_PIXELATION:
                return new CameraFilterPixelation(resources);

                case FILTER_TYPE_HUE:
                return new CameraFilterHue(resources);

                case FILTER_TYPE_GAMMA:
                return new CameraFilterGamma(resources);

        }

        return new CameraFilerNoChange(resources);
    }

    // 顶点着色器代码
    public static final String NO_FILTER_VERTEX_SHADER =
                    "uniform mat4 uMVPMatrix;\n" +
                    // 顶点坐标
                    "attribute vec4 aPosition;\n" +
                    "uniform mat4 uTexPMatrix;\n" +
                    // 纹理坐标
                    "attribute vec4 aTexCoordinate;\n" +

                    "varying vec2 vTexCoordinate;\n" +

                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTexCoordinate = (uTexPMatrix * aTexCoordinate).xy;\n" +
                    "}";

    // 片段着色器代码
    public static final String NO_FILTER_FRAGMENT_SHADER =
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES vTexture;\n" +
                    "varying vec2 vTexCoordinate;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(vTexture, vTexCoordinate);\n" +
                    "}\n";

    private final LinkedList<Runnable> mRunOnDraw = new LinkedList<>();

    /**
     * Shader程序中矩阵属性的句柄
     */
    private int uMVPMatrixHandle;
    private int texCoordinateHandle;
    private int vTextureHandle;
    private int vTexPMatrixHandle;

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
    // 纹理坐标缓冲区
    private FloatBuffer textureBuffer;

    // 此数组中每个顶点的坐标数
    private static final int COORDS_PER_VERTEX = 2;

    private int textureId;
    /**
     * 顶点坐标数组
     * 顶点坐标系中原点(0,0)在画布中心
     * 向右为x轴正方向
     * 向上为y轴正方向
     * 画布四个角坐标如下：
     * (-1, 1),(1, 1)
     * (-1,-1),(1,-1)
     */
    private float vertexCoords[] = {
            -1.0f,  1.0f,    // 左上
            -1.0f, -1.0f,    // 左下
             1.0f,  1.0f,    // 右上
             1.0f, -1.0f,    // 右下
    };

    /**
     * 纹理坐标数组
     * 这里我们需要注意纹理坐标系，原点(0,0s)在画布左下角
     * 向右为x轴正方向
     * 向上为y轴正方向
     * 画布四个角坐标如下：
     * (0,1),(1,1)
     * (0,0),(1,0)
     */
    private float textureCoords[] = {
            /**
             * 倒置的本质原因：使用这组纹理坐标会导致图片是倒置的。
             * 是因为Android中Bitmap生成纹理时，数据是从Bitmap左上角开始拷贝到纹理坐标 (0.0,0.0)，
             * 这样就会导致图片显示上下翻转180度
            */
            0.0f, 1.0f, // 左上
            0.0f, 0.0f, // 左下
            1.0f, 1.0f, // 右上
            1.0f, 0.0f, // 右下
            /**
             * 这里将上面的坐标上下镜像即可
            */
//            0.0f, 0.0f, // 左上
//            0.0f, 1.0f, // 左下
//            1.0f, 0.0f, // 右上
//            1.0f, 1.0f, // 右下
    };


    private short[] indexArray = {
            0, 1, 2,
            0, 2, 3
    };

    // 设置颜色为红色
    private float[] color = {1.0f, 0.0f, 0.0f, 1.0f};

    private final int vertexCount = vertexCoords.length / COORDS_PER_VERTEX;
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
    private Bitmap mBitmap;
    private String mVertexShader;
    private String mFragmentShader;

    public CameraFilterBase(Resources resources, String vertexShader, String fragmentShader) {
        this.mVertexShader = vertexShader;
        this.mFragmentShader = fragmentShader;
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.girl1);
        // 初始化形状坐标的顶点字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                vertexCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(vertexCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        ByteBuffer indexBB = ByteBuffer.allocateDirect(indexArray.length * 2);
        indexBB.order(ByteOrder.nativeOrder());
        indexBuffer = indexBB.asShortBuffer();
        indexBuffer.put(indexArray);
        indexBuffer.position(0);

        // 初始化纹理坐标顶点字节缓冲区
        textureBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureCoords);
        textureBuffer.position(0);
    }


    public void onSurfaceCreated() {
        mAdjuster = createAdjuster();
        // 创建纹理句柄
        textureId = createExternalOES();

        // 加载顶点着色器代码
        int vertexShader = GLESUtils.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShader);
        // 加载片段着色器代码
        int fragmentShader = GLESUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShader);

        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器添加到程序中
        GLES20.glAttachShader(mProgram, vertexShader);
        // 将片段着色器添加到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 链接OpenGL ES程序
        GLES20.glLinkProgram(mProgram);

        // 获取顶点着色器aPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        // 获取片段着色器vColor成员的句柄
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 获取绘制矩阵句柄
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 获取顶点着色器中纹理坐标的句柄
        texCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoordinate");
        vTexPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uTexPMatrix");
        vTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");
    }


    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // 获取原始矩阵，与原始矩阵相乘坐标不变
        Matrix.setIdentityM(mMVPMatrix, 0);
    }




    @Override
    public void drawFrame(float[] texMatrix) {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();

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
        // 启用纹理坐标控制句柄
        GLES20.glEnableVertexAttribArray(texCoordinateHandle);
        // 将纹理坐标变换矩阵传递给顶点着色器
        GLES20.glUniformMatrix4fv(vTexPMatrixHandle, 1, false, texMatrix, 0);
        // 写入坐标数据
        GLES20.glVertexAttribPointer(texCoordinateHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureBuffer);

        // 将缩放矩阵传递给着色器程序
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // 激活纹理编号0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        // 设置纹理采样器编号，该编号和glActiveTexture中设置的编号相同
        GLES20.glUniform1i(vTextureHandle, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // 取消绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        // 禁用顶点阵列
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordinateHandle);
    }

    private int createExternalOES() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // 取消绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }

    private int createTexture() {
        Log.i(TAG, "Bitmap:" + mBitmap);
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            // 取消绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            return texture[0];
        }
        return 0;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public int getTextureId() {
        return textureId;
    }

    @Override
    public void onSurfaceDestroyed() {
        GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
//        destroyFrameBuffers();
    }

    public int getUniformLocation(String uName) {
        return GLES30.glGetUniformLocation(mProgram, uName);
    }

    protected void runPendingOnDrawTasks() {
        synchronized (mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.removeFirst().run();
            }
        }
    }


    public void setUniformLocation(int location, float value) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(() -> GLES30.glUniform1f(location, value));
        }
    }
}


package com.zf.camera.trick.ui.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.zf.camera.trick.filter.sample.EBOTriangle;
import com.zf.camera.trick.utils.TrickLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageFilterSurfaceView extends GLSurfaceView {


    private Context mContext;
    private MyRenderer mMyRenderer;

    public ImageFilterSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public ImageFilterSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mMyRenderer = new MyRenderer(context);
        setEGLContextClientVersion(3);
        setRenderer(mMyRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    static class MyRenderer implements Renderer {

        private final EBOTriangle mTriangle;

        public MyRenderer(Context context) {
            mTriangle = new EBOTriangle();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mTriangle.surfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            TrickLog.d("A", "onSurfaceChanged, width = " + width + ", height = " + height);
            mTriangle.surfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mTriangle.draw();
        }
    }
}


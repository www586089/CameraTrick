package com.zf.camera.trick.ui.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;

import com.zf.camera.trick.filter.sample.IShape;
import com.zf.camera.trick.filter.sample.VAOTriangle;
import com.zf.camera.trick.utils.TrickLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageFilterSurfaceView extends GLSurfaceView {

    private static final String TAG = "A-Test";

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
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        TrickLog.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        TrickLog.d(TAG, "surfaceDestroyed");
        queueEvent(()-> mMyRenderer.onSurfaceDestroyed());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        TrickLog.d(TAG, "surfaceChanged");
    }

    public void updateShape(IShape shape) {
        queueEvent(()-> {mMyRenderer.updateShape(this, shape);});
    }


    static class MyRenderer implements Renderer {

        private IShape shape;

        public MyRenderer(Context context) {
            shape = new VAOTriangle(context);
        }

        private void updateShape(View surfaceView, IShape shape) {
            if (null != this.shape) {
                this.shape.onSurfaceDestroyed();
            }
            this.shape = shape;
            shape.onSurfaceCreated();
            shape.onSurfaceChanged(surfaceView.getWidth(), surfaceView.getHeight());
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            TrickLog.d(TAG, "onSurfaceCreated");
            shape.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            TrickLog.d(TAG, "onSurfaceChanged, width = " + width + ", height = " + height);
            shape.onSurfaceChanged(width, height);
        }

        public void onSurfaceDestroyed() {
            TrickLog.d(TAG, "onSurfaceDestroyed");
            shape.onSurfaceDestroyed();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            shape.drawFrame();
        }
    }
}


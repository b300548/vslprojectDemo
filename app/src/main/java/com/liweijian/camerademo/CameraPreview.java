package com.liweijian.camerademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/11/3.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mCamera.autoFocus(null);

        // 设置一个SurfaceHolder.Calback 监听 surface的创建和销毁
        mHolder = getHolder();
        mHolder.addCallback(this);

        // 不推荐使用 setting，但android3.0 之前需要设置
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 通知camera绘制预览
         try {
             mCamera.setPreviewDisplay(holder);
             mCamera.startPreview();
         }catch (IOException e){
             Log.d(TAG, "Error setting camera preview: " + e.getMessage());
         }

    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 如果预览设置可以改变，在这里修改事件
        // 在修改preview大小和格式之前要停止预览

        if(mHolder.getSurface() == null){
            // 预览的surface不存在
            return;
        }

        // 停止预览
        try {
            mCamera.stopPreview();
        }catch (Exception e){

        }

        // 在这里设置preview大小和调整大小，旋转或者重定格式

        // 使用新的设置开始预览
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }catch (Exception e){
            Log.d(TAG, "Error starting camera preview: "+ e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {


    }

}

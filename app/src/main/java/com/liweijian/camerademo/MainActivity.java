package com.liweijian.camerademo;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private FrameLayout mCameralayout;
    private Button captureButton;

    private Handler mHandler;
    private Bitmap newBitmap;

    private boolean isRecording = false;

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }



    private Bitmap getViewBitmap(View addViewContent) {

        addViewContent.setDrawingCacheEnabled(true);

        addViewContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0,
                addViewContent.getMeasuredWidth(),
                addViewContent.getMeasuredHeight());

        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        return bitmap;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap = rotateBitmapByDegree(bitmap, 90);


            //PhotoWithText view = new PhotoWithText(MainActivity.this);
            PhotoWithText view = new PhotoWithText(MainActivity.this);
            view.setBackground(bitmap);
            view.addText(MainActivity.this,"xs5x1sa561x56as1");

            Bitmap editBitmap = getViewBitmap(view);
            savePicture(editBitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoObtainCameraPermission();
        mHandler = new Handler();
//        // 创建一个Camera实例
//        mCamera = getCameraInstance();
//
//        // 创建Preview视图并添加到布局
//        mPreview = new CameraPreview(this,mCamera);
//        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
//        setCameraDisplayOrientation(this,Camera.CameraInfo.CAMERA_FACING_BACK,mCamera);
//
//        // 为拍照按钮设置监听器
//        captureButton = (Button)findViewById(R.id.btn_capture);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 从相机获取一张照片
//                mCamera.takePicture(null,null,mPicture);
//
//
//                // 录制视频
////                RecordVedio();
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder(); // 如果正在使用MediaRecorder，先释放
        releaseCamera(); // 释放Camera资源
    }

    // 检查手机是否具有摄像头
    private boolean checkCameraHardware(Context context){
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // 设备具有摄像头
            return true;
        }else {
            // 设备没有摄像头
            return false;
        }
    }

    // 获得Camera实例
    public static Camera getCameraInstance(){
        Camera camera = null;
        try{
            camera = Camera.open(); // 尝试获取Camera
        }catch (Exception e){
            // 如果Camera正在被使用，抛出异常
            Log.e(TAG, "failed to get camera" );
        }

        return camera;
    }

    // 准备视频录制,必须按照以下顺序
    private boolean prepareVedioRecorder(){
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // 步骤1: unlock Camera 和为MediaRecorder设置camera
        setCameraDisplayOrientation(this,Camera.CameraInfo.CAMERA_FACING_BACK,mCamera);
        mCamera.autoFocus(null);
        mCamera.unlock(); // Android 4.0后可以不调用
        mMediaRecorder.setCamera(mCamera);

        // 步骤2: 设置资源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Android 2.2(API 8)之前使用以下代码设置
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);


        // 步骤3: 设置CamcorderProfile（需要API 8及以上）
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // 步骤4: 设置输出文件
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // 步骤5: 设置预览输出
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());


        // 步骤6: 准备配置MediaRecorder
        try{
            mMediaRecorder.prepare();
        }catch (IllegalStateException e){
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }catch (IOException e){
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    // 录制视频
    private void RecordVedio(){
        if (isRecording) {
            // 停止录制并释放camera
            mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();

            isRecording = false;
        }else {
            // 初始化camera
            if(prepareVedioRecorder()){
                // Camera可以使用并unlocked， MediaRecorder已经准备好，
                mMediaRecorder.start();
                mHandler.post(new capturePhoto());

                isRecording = true;
            }else {
                // MediaRecorder没有准备好，释放MediaRecorder
                releaseMediaRecorder();
                Log.d(TAG, "MediaRecorder not prepared");
            }
        }
    }

    // 释放MediaRecorder资源
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null){
            mMediaRecorder.reset(); // 清除recorder配置
            mMediaRecorder.release(); // 释放recorder对象
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    // 释放Camera资源
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    // 为保存图片或视频创建一个文件uri
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // 为保存图片或视频创建一个文件
    private static File getOutputMediaFile(int type){
        // 为了安全，应该检查是否具有SD卡
        // 在此之前，使用Environment.getExternalStorageState()

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MyCameraApp");

        // 如果不存在，创建储存目录
        if (!mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // 创建media文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp +".jpg");
            Log.d(TAG, mediaFile.toString());
        }else if (type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }else {
            return null;
        }

        return mediaFile;
    }


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void autoObtainCameraPermission() {
        Log.i("test","自动获取相机权限");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //ToastUtils.showShort(this, "您已经拒绝过一次");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            if (!checkCameraHardware(this)) {
                Toast.makeText(MainActivity.this, "相机不支持", Toast.LENGTH_SHORT)
                        .show();
            } else {
                mCamera = getCameraInstance();
                mPreview = new CameraPreview(MainActivity.this, mCamera);
                mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
                mCameralayout.addView(mPreview);
                // openCamera();
                captureButton = (Button)findViewById(R.id.btn_capture);
                captureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecordVedio();
                    }
                });
                setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            }
            //有权限直接调用系统相机拍照
//            if (hasSdcard()) {
//                Log.i("test","已经获取了相机权限");
//                imageUri = Uri.fromFile(fileUri);
//                //通过FileProvider创建一个content类型的Uri
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Log.i("fileuri",fileUri.toString());
//                    imageUri = FileProvider.getUriForFile(MainActivity.this, "com.liweijian.fileproviderdemo", fileUri);
//                }
//                PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
//            } else {
//                ToastUtils.showShort(this, "设备没有SD卡！");
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if (!checkCameraHardware(this)) {
                    Toast.makeText(MainActivity.this, "相机不支持", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mCamera = getCameraInstance();
                    mPreview = new CameraPreview(MainActivity.this, mCamera);
                    mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
                    mCameralayout.addView(mPreview);
                    //openCamera();
                    captureButton = (Button)findViewById(R.id.btn_capture);
                    captureButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RecordVedio();
                        }
                    });
                    setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
                }
                break;
        }
    }

    private void savePicture(Bitmap bitmap){
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        if(pictureFile == null){
            Log.d(TAG, "Error creating media file");
            return;
        }

        try{
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            //fos.write(bitmap);
            fos.flush();
            fos.close();
        }catch (FileNotFoundException e){
            Log.d(TAG, "File not found: " + e.getMessage());
        }catch (IOException e){
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }
    class capturePhoto extends Thread{
        @Override
        public void run() {
            super.run();

            try {
                Thread.sleep(2000);
                mCamera.takePicture(null,null,mPicture);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}

package com.example.blindproject3;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

//SurfaceView를 사용해서 카메라 미리보기를 추가
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    //SurfaceView는 껍데기 역할만 하고 컨트롤은  SurfaceHolder가 담당
    SurfaceHolder holder;
    Camera camera = null;
    private Camera.CameraInfo cameraInfo;
    private int mDisplayOrientation;
    public CameraSurfaceView(Context context) {
        super(context);

        init(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context){
        //초기화를 위한 메소드
        holder = getHolder();
        holder.addCallback(this);
        //처음 디스플레이 방향 얻기
        mDisplayOrientation=((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
    }

    //SurfaceView가 만들어지는 시점에 호출
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //카메라 객체 오픈
        camera = Camera.open();
        cameraInfo=new Camera.CameraInfo();
        Camera.getCameraInfo(0,cameraInfo);//0:camera facing back 1:camera facing front

        try {
            //카메라 객체에 SurfaceView를 미리보기로 사용
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // SurfaceViw 가 변경되는 시점에 호출, 화면에 보여지기 전에 크기를 결정
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int orientation=calculatePreviewOrientation(cameraInfo,mDisplayOrientation);
        camera.setDisplayOrientation(orientation);
        camera.startPreview(); // 미리보기 화면에 렌즈로부터 들어온 영상을 뿌려줌
    }

    //SurfaceView가 소멸하는 시점에 호출
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview(); //미리보기중지
        camera.release(); //리소스 해제
        camera = null;
    }

    //사진 찍기
    public boolean capture(Camera.PictureCallback callback){
        if(camera != null){
            camera.takePicture(null, null, callback);
            return true;
        } else {
            return false;
        }
    }

    //
    public int calculatePreviewOrientation(Camera.CameraInfo info, int rotation){
        int degrees=0;
        switch(rotation){
            case Surface.ROTATION_0:
                degrees=0;
                break;
            case Surface.ROTATION_90:
                degrees=90;
                break;
            case Surface.ROTATION_180:
                degrees=180;
                break;
            case Surface.ROTATION_270:
                degrees=270;
                break;

        }
        int result=90;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result=(info.orientation+degrees)%360;
            result=(360-result)%360;//좌우반전 보정
        }
        return result;
    }
}
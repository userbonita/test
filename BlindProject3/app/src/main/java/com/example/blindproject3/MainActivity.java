package com.example.blindproject3;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {

    private int port = ;//포트번호
    private String addr = "";//서버 ip

    public static final int REQUEST_PERMISSION = 11;

    private Button btn_submit;
    private ImageView img1;
    private TextView text;
    private ImageView imageView;
    private CameraSurfaceView surfaceView;

    private FileOutputStream output=null;
    private Bitmap resizedBitmap=null;


    //Handler handler=new Handler();//토스트를 띄우기 위한 메인스레드 핸들러 객체 생성


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //파일 생성-경로 확인하기 위한

        //파일 권한 묻기
        checkPermission();
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
*/
        btn_submit = findViewById(R.id.btn_submit);
        img1 = findViewById(R.id.img1);
        text = findViewById(R.id.text);
        imageView = findViewById(R.id.imageView);
        surfaceView = findViewById(R.id.surfaceView);
        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라 사진 캡쳐
                capture();
            }

        });




        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("시작");

                SocketThread thread = new SocketThread(addr);
                //thread.run();
                thread.start();
            }
        });

    }

    public void capture() {
        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //사진 찍은 결과가 byte[]로 전달된 것을 이미지뷰로 보여주기
                //bytearray 형식으로 전달
                //이걸이용해서 이미지뷰로 보여주거나 파일로 저장
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8; // 8분의 1크기로 비트맵 객체 생성
                // 가져온 결과물을 비트맵 객체로 생성
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);


                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newWidth = 640;
                int newHeight = 640;

                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;

                Matrix matrix = new Matrix();

                matrix.postScale(scaleWidth, scaleHeight);

                matrix.postRotate(90);


                //Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                //BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
                imageView.setImageDrawable(new BitmapDrawable(resizedBitmap));//이미지뷰에 사진 보여주기

                File storageDir = new File( getFilesDir()+"/capture");
                //File storageDir = new File( "/sdcard/Pictures");
                if (!storageDir.exists()) //폴더가 없으면 생성.
                    storageDir.mkdirs();

                text.setText("sto"+storageDir.exists()+"");

                String filename = "toserver" + ".jpg";
                File file=new File(storageDir,filename);
                boolean deleted = file.delete();
                text.setText(file.isFile()+"");


                //이미지 파일 저장
                try {

                    output=new FileOutputStream(file);
                    text.setText(file.isFile()+"?");
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG,70,output);


                    // BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    // Bitmap bitmap1 = drawable.getBitmap();


                }
                catch (Exception e){
                    text.setText("파일 오류");
                }
                finally {
                    try{
                        output.close();
                    }catch (IOException e){

                    }
                }

                //output파일 확인
                text.setText("사진 파일 확인");

                try {
                    //Bitmap bitmap1 = BitmapFactory.decodeFile(storageDir+"/toserver.jpg");
                    Bitmap bitmap1 = BitmapFactory.decodeStream(new FileInputStream(file));
                    img1.setImageBitmap(bitmap1);
                }catch (IOException e){

                }

                // 이미지뷰에 비트맵 객체 설정
                //imageView.setImageBitmap(bitmap);
                // 사진을 찍으면 미리보기가 중지되기 때문에 다시 시작하게 함
                camera.startPreview();
            }
        });


    }


    class SocketThread extends Thread {
        String host; //서버 IP
        String data; //전송 데이터
        //String filePath = "/storage/emulated/0/Pictures/toserver.jpg";
        //String filePath = "/sdcard/Pictures/toserver.jpg";
        Socket socket;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedInputStream bis;
        ObjectInputStream instream;

        public SocketThread(String host) {
            this.host = host;
        }

        //@Override
        public void run() {
            try {

                socket = new Socket(host, port);//소켓 열어주기
                text.setText("소켓 열기 성공");
                text.setText("사진 전송 준비");

                sendimg();

                socket.close();//소켓 해제
            } catch (IOException e) {
                text.setText("IOException1 오류");

            }
        }


        public void sendimg() {
            try {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);

                //byteArray.flush();
                byte[] bytes = byteArray.toByteArray();
                text.setText(" " + bytes.toString());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                dos.writeUTF(Integer.toString(bytes.length)); //파일 크기 보냄
                dos.flush();

                dos.write(bytes);
                dos.flush();

                //--><-- 파이썬에서 결과받기
                bytes = new byte[20];
                dis.read(bytes, 0, 20);
                ByteBuffer b1 = ByteBuffer.wrap(bytes);
                b1.order(ByteOrder.LITTLE_ENDIAN);
                int length = b1.getInt();
                bytes = new byte[length];
                dis.read(bytes, 0, length);
                String msg = new String(bytes, "UTF-8");
                text.setText("서버에서 온 결과 " + msg);

                dos.close();
                dis.close();

            } catch (IOException e) {
            }


        }
    }


    @Override
    public void onResume() {
        super.onResume();
        checkPermission(); //권한체크
    }

    //권한 확인
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //권한이 없으면 권한 요청
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // 권한이 취소되면 result 배열은 비어있다.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "권한 확인", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();
                    finish(); //권한이 없으면 앱 종료
                }
            }
        }
    }


}


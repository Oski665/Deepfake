package com.example.deepfake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;


public class ImageProcessing extends AppCompatActivity {

    ImageView ivPreProc;
    VideoView videoView;
    MaterialButton btnRoteRight, btnRoteLeft, btnLoadVid;
    Button btnDetect;
    Mat matrix;
    String imagePathLoaded, videoPathLoaded;
    MediaController mediaController;
    float angle = 0;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();

        setTheme(R.style.Theme_Deepfake);
        setContentView(R.layout.activity_image_processing);

        ivPreProc = findViewById(R.id.ivPreProccesing);
        videoView = findViewById(R.id.vvImgProcessing);
        btnDetect = findViewById(R.id.btnFaceDetection);
        btnRoteRight = findViewById(R.id.btnRotateRight);
        btnRoteLeft = findViewById(R.id.btnRotateLeft);
        btnLoadVid = findViewById(R.id.btnLoadVideo);

        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("image");

        Uri fileUri = Uri.parse(imagePath);
        ivPreProc.setImageURI(fileUri);

        imagePathLoaded = getAbsolutePath(fileUri);

        matrix = Imgcodecs.imread(imagePathLoaded);

        btnRoteRight.setOnClickListener(new View.OnClickListener(){
           @SuppressLint("SetTextI18n")
           @Override
            public void onClick(View view){
               angle+=90;
               rotateImage(ivPreProc,angle);
               matrix = rotateRight(matrix);
               flag = 1;
           }
        });

        btnRoteLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                angle-=90;
                rotateImage(ivPreProc,angle);
                matrix = rotateLeft(matrix);
                flag = 1;
            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag == 1){
                    detectFace(matrix);
                    rotateImage(ivPreProc,angle+90);
                }
                else{
                    detectFace(matrix);
                }
            }
        });

        btnLoadVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoIntent = new Intent(Intent.ACTION_PICK);
                videoIntent.setType("video/*");
                startActivityForResult(videoIntent,1);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Uri videoUri = data.getData();
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(PreparedListener);
            videoView.start();
            videoPathLoaded = getAbsolutePath(videoUri);
        }
    }

    MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener(){
        @Override
        public void onPrepared(MediaPlayer m) {
            try {
                if (m.isPlaying()) {
                    m.stop();
                    m.release();
                    m = new MediaPlayer();
                }
                m.setVolume(0f, 0f);
                m.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public String getAbsolutePath(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, proj, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    public void detectFace(Mat matrix){
        Imgproc.cvtColor(matrix, matrix,Imgproc.COLOR_RGB2BGRA);

        Mat mat = matrix.clone();
        CascadeClassifier cascadeClassifier = new CascadeClassifier();

        try{
            InputStream is = this.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1){
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        }catch(Exception e){
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

        MatOfRect faceArray = new MatOfRect();
        cascadeClassifier.detectMultiScale(mat,faceArray);


        for(Rect face : faceArray.toArray()){
            Imgproc.rectangle(mat,
                    new Point(face.x, face.y),
                    new Point(face.x + face.width,face.y + face.height * 1.3),
                    new Scalar(3, 182, 252),5);
        }

        Mat finalMatrix = mat.clone();
        Bitmap bitmap = Bitmap.createBitmap(finalMatrix.cols(),finalMatrix.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(finalMatrix,bitmap);
        ivPreProc.setImageBitmap(bitmap);

    }
    public static Mat rotateRight(Mat src)
    {
        Mat dst = new Mat();
        Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);
        return dst;
    }

    public static Mat rotateLeft(Mat src)
    {
        Mat dst = new Mat();
        Core.rotate(src, dst, Core.ROTATE_90_COUNTERCLOCKWISE);
        return dst;
    }

    public void rotateImage(ImageView iv, float ang){
        iv.setPivotX(iv.getWidth()/2);
        iv.setPivotY(iv.getHeight()/2);
        iv.setRotation(ang);
    }

}
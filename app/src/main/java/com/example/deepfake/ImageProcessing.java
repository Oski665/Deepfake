package com.example.deepfake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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

    ImageView ivPreProc, ivPostProc;
    Button btnDetect, btnRote;
    Mat matrix;
    String imagePathLoaded;
    int angle = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setTheme(R.style.Theme_Deepfake);
        setContentView(R.layout.activity_image_processing);

        ivPreProc = findViewById(R.id.ivPreProccesing);
        ivPostProc = findViewById(R.id.ivPostProccesing);
        btnDetect = findViewById(R.id.btnProccesing);
        btnRote = findViewById(R.id.btnRotate);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("image");


        Uri fileUri = Uri.parse(imagePath);
        ivPreProc.setImageURI(fileUri);

        imagePathLoaded = getAbsolutePath(fileUri);
        matrix = Imgcodecs.imread(imagePathLoaded);

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectFace(matrix);
            }
        });

        btnRote.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View view){
               ivPreProc.setRotation(angle);
               angle+=90;
           }
        });
    }
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

        int numFaces = faceArray.toArray().length;
        for(Rect face : faceArray.toArray()){
            Imgproc.rectangle(mat,
                    new Point(face.x,face.y),
                    new Point(face.x + face.width,face.y + face.height),
                    new Scalar(3, 182, 252),40);
        }

        Mat finalMatrix = mat.clone();
        Bitmap bitmap = Bitmap.createBitmap(finalMatrix.cols(),finalMatrix.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(finalMatrix,bitmap);
        ivPostProc.setImageBitmap(bitmap);

    }

}
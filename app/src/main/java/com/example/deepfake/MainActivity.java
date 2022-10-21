package com.example.deepfake;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1234;
    private static final int CAPTURE_CODE = 1001;
    public static final int GALLERY_REQUEST_CODE = 1235;
    ImageView imageView;
    FloatingActionButton btnOpenCamera, btnOpenGallery;
    Uri imageUri, tempImg;
    LinearLayout linearLayout;
    String uriBgLogo= "@drawable/pbslogo";
    Drawable backgroundLogo, imageShapes, holdFirst, holdSecond;
    int flag = 0;
    TextView textView;
    CardView cardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Deepfake);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.ivCamera);
        btnOpenCamera = findViewById(R.id.fbtOpenCamera);
        btnOpenGallery = findViewById(R.id.fbtOpenGallery);
        linearLayout = findViewById(R.id.historyGallery);
        textView = findViewById(R.id.tvUploadImage);
//        cardView = findViewById(R.id.cvDynamic);


        int imgResource = getResources().getIdentifier(uriBgLogo, null, getPackageName());
        backgroundLogo = getResources().getDrawable(imgResource);

        btnOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else{
                        openCamera();
                    }
                }
                else{
                    openCamera();
                }
            }
        });

        btnOpenGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });


            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(imageView.getDrawable() != null) {
                        Toast.makeText(MainActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                        textView.setText("Upload image to view");
                        imageView.setImageResource(0);
                        flag = 0;
                    }
                    return true;
                }
            });

    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"new image");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camintent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(camintent,CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CAPTURE_CODE){
            if(resultCode == RESULT_OK){
                if(flag == 1){
                    holdPict(tempImg);
                    imageView.setImageURI(imageUri);
                    flag = 0;
                }

                if(flag == 0){
                    saveTempCam();
                    flag = 1;
                }
            }
        }
        if(requestCode == GALLERY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag","onActivityResult: Gallery Image Uri: " + imageFileName);
                if(flag == 1){
                    holdPict(tempImg);
                    imageView.setImageURI(contentUri);
                    textView.setText("Current choice");
                    flag = 0;
                }

                if(flag == 0){
                    saveTempGall(contentUri);
                    textView.setText("Current choice");
                    flag = 1;
                }
            }
        }
    }

    public void saveTempCam(){
        imageView.setImageURI(imageUri);
        tempImg = imageUri;
    }
    public void saveTempGall(Uri contentUri){
        imageView.setImageURI(contentUri);
        tempImg = contentUri;
    }

    public void addImgToHistry(ImageView imgView, int width, int height){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(12,0,12,0);
        imgView.setLayoutParams(layoutParams);
        imgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(imgView.getDrawable() != null) {
                    Toast.makeText(MainActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                    imgView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(imageView.getDrawable() != imageView.getResources().getDrawable(R.drawable.pbslogo)){
                if(imageView.getDrawable() != null){
                    holdFirst = imgView.getDrawable();
                    holdSecond = imageView.getDrawable();
                    imgView.setImageDrawable(holdSecond);
                    imageView.setImageDrawable(holdFirst);
                }
                else{
                    holdFirst = imgView.getDrawable();
                    imageView.setImageDrawable(holdFirst);
                    imgView.setVisibility(View.GONE);
                    flag = 1;
                }
            }
        });
        //cardView.addView(imgView);
        linearLayout.addView(imgView);
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    public void holdPict(Uri tempUri){
        ImageView imgView = new ImageView(MainActivity.this);
//        imgView.setImageResource(R.drawable.dynamicimageshape);
        imgView.setImageURI(tempImg);
        addImgToHistry(imgView,180,270);
    }

}
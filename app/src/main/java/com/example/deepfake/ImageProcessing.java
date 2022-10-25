package com.example.deepfake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.Serializable;


public class ImageProcessing extends AppCompatActivity {

    ImageView ivProc;
    Uri imgVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Deepfake);
        setContentView(R.layout.activity_image_processing);

        ivProc = findViewById(R.id.ivProccesing);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("image");
        Uri fileUri = Uri.parse(imagePath);
        ivProc.setImageURI(fileUri);

//        Intent intent = getIntent();
//        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("picture");
//        ivProc.setImageBitmap(bitmap);
    }
}
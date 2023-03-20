package com.example.deepfake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;


public class ImageProcessing extends AppCompatActivity {
    Context context;
    ImageView ivPreProc;
    VideoView videoView;
    MaterialButton btnRoteRight, btnRoteLeft, btnLoadVid, btnDetect, btnDeepfake;
    Mat matrix;
    String imagePathLoaded, videoPathLoaded;
    MediaController mediaController;
    TextView tv;
    float angle = 0;
    int flag = 0;
    @SuppressLint("WrongThread")
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
        btnDeepfake = findViewById(R.id.btnCreateDeepfake);

        tv = findViewById(R.id.textView);
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("image");
        Uri fileUri = Uri.parse(imagePath);
        ivPreProc.setImageURI(fileUri);
        imagePathLoaded = getAbsolutePath(fileUri);
        matrix = Imgcodecs.imread(imagePathLoaded);

        btnRoteRight.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                angle += 90;
                rotateImage(ivPreProc, angle);
                matrix = rotateRight(matrix);
                flag = 1;
            }
        });

        btnRoteLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                angle -= 90;
                rotateImage(ivPreProc, angle);
                matrix = rotateLeft(matrix);
                flag = 1;
            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    detectFace(matrix);
                    rotateImage(ivPreProc, angle + 90);
                } else {
                    detectFace(matrix);
                }
            }
        });

        btnLoadVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoIntent = new Intent(Intent.ACTION_PICK);
                videoIntent.setType("video/*");
                startActivityForResult(videoIntent, 1);

            }
        });

        btnDeepfake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadVideoFile("http://192.168.51.27:5000/send_video");
            }
        });
    }

    public void downloadVideoFile(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                checkPerm();
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File file = new File(directory, "result.mp4");
                InputStream is = response.body().byteStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                is.close();

                Intent intent = new Intent(ImageProcessing.this, DeepfakeDownload.class);
                intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                startActivity(intent);
            }
        });
    }

    public void checkPerm(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
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
            OkHttpClient client = new OkHttpClient();
            context = videoView.getContext();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePathLoaded, options);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            ContentResolver contentResolver = context.getContentResolver();
            final String contentType = contentResolver.getType(videoUri);
            final AssetFileDescriptor fd;
            try {
                fd = contentResolver.openAssetFileDescriptor(videoUri, "r");
                if (fd == null) {
                    try {
                        throw new FileNotFoundException("could not open file descriptor");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                RequestBody videoFile = new RequestBody() {
                    @Override public long contentLength() { return fd.getDeclaredLength(); }
                    @Override public MediaType contentType() { return MediaType.parse(contentType); }
                    @Override public void writeTo(BufferedSink sink) throws IOException {
                        try (InputStream is = fd.createInputStream()) {
                            sink.writeAll(Okio.buffer(Okio.source(is)));
                        }
                    }
                };
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                        .addFormDataPart("video", "androidFlaskVideo.mp4", videoFile)
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.51.27:5000/")
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        try {
                            fd.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText("Przesłano pliki na serwer");
                                }
                            });
                        } catch (IOException ex) {
                            e.addSuppressed(ex);
                        }
                    }
                    @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        fd.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText("image and video uploaded");
                            }
                        });
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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
    void postRequest(RequestBody postBody) {
        OkHttpClient client2 = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.51.27:5000/")
                .post(postBody)
                .build();

        client2.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("Failed to Connect to Server");
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("image uploaded");
                    }
                });
            }
        });
    }
}
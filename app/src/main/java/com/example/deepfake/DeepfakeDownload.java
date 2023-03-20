package com.example.deepfake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class DeepfakeDownload extends AppCompatActivity {

    VideoView deepfakeVideo;
    EditText etTTS;
    MaterialButton btnTTS;
    TextToSpeech t1;
    SeekBar sbFast, sbPitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Deepfake);
        setContentView(R.layout.activity_deepfake_download);

        deepfakeVideo = findViewById(R.id.finalDeepfakeView);
        btnTTS = findViewById(R.id.btnTextToSpeach);
        etTTS = findViewById(R.id.etTextToSpeach);
        sbFast = findViewById(R.id.sbLevelOfFast);
        sbPitch = findViewById(R.id.sbLevelOfPitch);

        Intent intent = getIntent();
        Uri videoUri = intent.getData();

        deepfakeVideo.setVideoURI(videoUri);
        deepfakeVideo.start();

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        btnTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = etTTS.getText().toString();
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                addTextFromEditTextToTextToSpeech(etTTS, t1);
                replayVideo(deepfakeVideo);
            }
        });
    }

    public void addTextFromEditTextToTextToSpeech(EditText editText, TextToSpeech textToSpeech) {
        String text = editText.getText().toString();
        float pitch = (float)sbPitch.getProgress() / 50;
        if(pitch < 0.1) pitch = 0.1f;
        float fast = (float)sbFast.getProgress() / 50;
        if(fast < 0.1) fast = 0.1f;
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(fast);
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    public void replayVideo(VideoView videoView) {
        videoView.start();
    }

}

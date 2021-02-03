package com.qbqb.todayszone;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class SplashActivity extends AppCompatActivity {

    VideoView vdTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splashscreen);

        vdTitle = (VideoView)findViewById(R.id.vdTitle);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.title);
        vdTitle.setVideoURI(video);
        vdTitle.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isFinishing())
                    return;

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                Animatoo.animateFade(SplashActivity.this);
                finish();
            }
        });
        vdTitle.start();
    }
}

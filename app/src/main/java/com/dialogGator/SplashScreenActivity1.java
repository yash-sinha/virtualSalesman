package com.dialogGator;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.voice.TTS;

public class SplashScreenActivity1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen1);
        TTS.init(getApplicationContext());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SplashScreenActivity1.this,MainActivity.class);
                SplashScreenActivity1.this.startActivity(intent);
                SplashScreenActivity1.this.finish();

            }
        }, 1000);
    }
}

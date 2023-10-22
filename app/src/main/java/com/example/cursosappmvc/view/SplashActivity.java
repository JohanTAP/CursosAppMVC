package com.example.cursosappmvc.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.MainActivity;
import com.example.cursosappmvc.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Añade una transición de fundido
                finish();
            }
        }, SPLASH_DURATION);
    }

}
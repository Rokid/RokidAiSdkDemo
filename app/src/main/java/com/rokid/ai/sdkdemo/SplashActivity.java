package com.rokid.ai.sdkdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rokid.ai.basic.AudioAiConfig;
import com.rokid.ai.sdkdemo.service.TipsService;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_phone_audio:
                beforeClick();
                startActivity(new Intent(this, PhoneAudioActivity.class));
                break;
            case R.id.btn_bsp_stop_server:
                beforeClick();
                break;
            default:
                break;
        }
    }


    public void beforeClick() {
        try {
            Intent intent = new Intent(this, TipsService.class);
            getApplication().stopService(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            Intent intent = AudioAiConfig.getIndependentIntent(this);
            getApplication().stopService(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

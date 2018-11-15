package com.rokid.ai.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rokid.ai.basic.AudioAiConfig;
import com.rokid.ai.sdkdemo.service.TipsService;

public class SplashActivity extends AppCompatActivity {

    private Button btnMain;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        btnMain = findViewById(R.id.btn_main);
    }

    private void repeatAct(){
        if (count < 3) {
            count = count + 1;
            btnMain.postDelayed(new Runnable() {
                @Override
                public void run() {
                    repeatAct();
                }
            }, 5000);
            Intent intent = new Intent(SplashActivity.this,MainActivity.class);
            intent.putExtra(MainActivity.PARAM_TEST_CODE, count);
            startActivity(intent);
        } else {
            count = 0;
        }
    }

    public void onClick(View view) {

        beforeClick();

        switch (view.getId()) {
            case R.id.btn_main:
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                break;
            case R.id.btn_test_s:
                startActivity(new Intent(SplashActivity.this, TestActivity.class));
                break;
            case R.id.btn_other_audio:
                startActivity(new Intent(SplashActivity.this, OtherAudioActivity.class));
                break;
            case R.id.btn_bsp_audio:
                startActivity(new Intent(SplashActivity.this, BspAudioActivity.class));
                break;
            case R.id.btn_main_igore_volume:
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.IGNORE_SUPPRESS_AUDIO_VOLUME, true);
                startActivity(intent);
                break;
            case R.id.btn_repeat:
                repeatAct();
                break;
            case R.id.btn_phone_test_splash_activity:
                startActivity(new Intent(this, PhoneTestActivity.class));
                break;
            case R.id.btn_phone_test_new_sdk_splash_activity:
                startActivity(new Intent(this, PhoneAudioNewSDKActivity.class));
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
}

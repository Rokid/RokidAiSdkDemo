package com.rokid.ai.sdkdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity {
    private Button btnMain;
    private Button btnTestS;

    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        btnMain = findViewById(R.id.btn_main);
        btnTestS = findViewById(R.id.btn_test_s);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
            }
        });
        btnTestS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this,TestActivity.class));
            }
        });
        findViewById(R.id.btn_other_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, OtherAudioActivity.class));
            }
        });
        findViewById(R.id.btn_main_igore_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                intent.putExtra(MainActivity.IGNORE_SUPPRESS_AUDIO_VOLUME, true);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_repeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatAct();
            }
        });
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

        Intent it ;
        switch (view.getId()) {
            case R.id.btn_phone_test_splash_activity:
                Log.d("theonClick", "the onClick btn_phone_test_splash_activity is called");
                it = new Intent(this, PhoneTestActivity.class);
                startActivity(it);
                break;
            case R.id.btn_phone_test_new_sdk_splash_activity:
                Log.d("theonClick", "the onClick btn_phone_test_new_sdk_splash_activity is called");
                it = new Intent(this, PhoneAudioNewSDKActivity.class);
                startActivity(it);
                break;

        }
    }
}

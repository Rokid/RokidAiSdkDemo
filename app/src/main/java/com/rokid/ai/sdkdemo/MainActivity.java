package com.rokid.ai.sdkdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.ai.audioai.AudioAiConfig;
import com.rokid.ai.audioai.aidl.IRokidAudioAiListener;
import com.rokid.ai.audioai.aidl.IRokidAudioAiService;
import com.rokid.ai.audioai.aidl.ServerConfig;
import com.rokid.ai.audioai.socket.business.preprocess.IReceiverPcmListener;
import com.rokid.ai.audioai.socket.business.preprocess.PcmClientManager;
import com.rokid.ai.audioai.util.Logger;
import com.rokid.ai.sdkdemo.util.PerssionManager;

import com.rokid.ai.sdkdemo.presenter.AsrControlPresenter;
import com.rokid.ai.sdkdemo.presenter.AsrControlPresenterImpl;
import com.rokid.ai.sdkdemo.view.IAsrUiView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String IGNORE_SUPPRESS_AUDIO_VOLUME = "Ignore_Suppress_Audio_Volume";
    public static final String PARAM_TEST_CODE = "PARAM_TEST_CODE";
    private final static String TAG = MainActivity.class.getName();
    private final static String CONFIG_FILE_NAME = "Rokid_Ai_SDK_Config.txt";
    private Context mContext;

    private TextView mAsrStateTV;
    private TextView mAsrResultTv;
    private TextView mNLPTv;
    private TextView mActionTv;
    private TextView mActivationTv;
    private TextView mErrorTv;
    private TextView mPcmTv;


    private Handler mHander = new Handler();
    private AsrControlPresenter mAsrControlPresenter;

    private IRokidAudioAiService mAudioAiService;
    private int mActivationCount;
    private long mPcmCount;
    private int mBadPcmCount;

    private int mTestCode;

    private boolean mIgnoreSuppressAudioVolume = false;

    private Intent mServiceIntent;

    private PcmClientManager mPcmClientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mActivationCount = 0;
        mPcmCount = 0;

        mIgnoreSuppressAudioVolume = getIntent().getBooleanExtra(IGNORE_SUPPRESS_AUDIO_VOLUME, false);
        mTestCode = getIntent().getIntExtra(PARAM_TEST_CODE, 0);

        initView();

        requestPermission();

        mServiceIntent = AudioAiConfig.getIndependentIntent(this);
        bindService(mServiceIntent, mAiServiceConnection, BIND_AUTO_CREATE);

        mPcmClientManager = new PcmClientManager();

        mAsrControlPresenter = new AsrControlPresenterImpl(this, mAsrUiView);

    }

    public void initView() {
        mAsrStateTV = findViewById(R.id.main_ast_state_tv);
        mAsrResultTv = findViewById(R.id.main_ast_result_tv);
        mNLPTv = findViewById(R.id.main_ast_npl_tv);
        mActionTv = findViewById(R.id.main_ast_action_tv);
        mActivationTv = findViewById(R.id.main_ast_activation_tv);
        mErrorTv = findViewById(R.id.main_ast_error_tv);
        mPcmTv = findViewById(R.id.main_ast_pcm_tv);


        findViewById(R.id.start_play_tts_btn).setOnClickListener(this);
        findViewById(R.id.start_play_media_btn).setOnClickListener(this);
        findViewById(R.id.stop_play_media_btn).setOnClickListener(this);

        ((TextView) findViewById(R.id.main_test_tv)).setText("ACT : " + mTestCode);
    }


    private ServiceConnection mAiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                mAudioAiService = IRokidAudioAiService.Stub.asInterface(service);
                try {
                    mAudioAiService.srartAudioAiServer(getServiceConfig(), mAudioAiListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAudioAiService = null;
        }
    };

    private void showPcmData(final long len, byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPcmCount = mPcmCount + 1;
                if (len < 100) {
                    mBadPcmCount = mBadPcmCount + 1;
                }

                if (mPcmTv != null) {
                    mPcmTv.setText("pcm流数：" + mPcmCount + "         数据Len：" + len
                                    + "       错误数据：" + mBadPcmCount);
                }
            }
        });
    }

    private IRokidAudioAiListener mAudioAiListener = new IRokidAudioAiListener.Stub() {

        @Override
        public void onIntermediateSlice(String asr) throws RemoteException {
            Logger.d(TAG, "onIntermediateSlice(): asr = " + asr);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrResultText(asr, false);
            }
        }

        @Override
        public void onIntermediateEntire(String asr) throws RemoteException {
            Logger.d(TAG, "onIntermediateEntire(): asr = " + asr);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrResultText(asr, true);
            }
        }

        @Override
        public void onCompleteNlp(String nlp, String action) throws RemoteException {
            Logger.d(TAG, "onCompleteNlp(): nlp = " + nlp + " action = " + action);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrNlpText(nlp, action);
            }
        }

        @Override
        public void onVoiceEvent(int event, float sl, float energy) throws RemoteException {
//            Logger.d(TAG, "onVoiceEvent Thread：" + Thread.currentThread().getName());
            Logger.d(TAG, "onVoiceEvent(): event = " + event + ", sl = " + sl + ", energy = " + energy);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrEvent(event, sl, energy);
            }
        }

        @Override
        public void onRecognizeError(int errorCode) throws RemoteException {
            Logger.d(TAG, "onRecognizeError(): errorCode = " + errorCode);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showRecognizeError(errorCode);
            }
        }

        @Override
        public void onServerSocketCreate(String ip, int post) throws RemoteException {

        }

        @Override
        public void onPcmServerPrepared() throws RemoteException {
            if (mPcmClientManager != null) {
                mPcmClientManager.startSocket(null, mPcmReceiver);
            }
        }
    };

    private IReceiverPcmListener mPcmReceiver = new IReceiverPcmListener() {
        @Override
        public void onPcmReceive(int length, byte[] data) {
            showPcmData(length, data);
        }
    };

    private IAsrUiView mAsrUiView = new IAsrUiView() {

        @Override
        public void showAsrResultText(final String str) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAsrResultTv != null) {
                        mAsrResultTv.setText(str);
                    }
                }
            });
        }

        @Override
        public void showAsrStateText(final String str) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAsrStateTV != null) {
                        mAsrStateTV.setText(str);
                    }
                }
            });
        }

        @Override
        public void showAsrActivation(final boolean isActivation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivationCount = mActivationCount + 1;
                    if (mActivationTv != null) {
                        mActivationTv.setText("激活访问次数：" + mActivationCount);
                    }
                    if (isActivation) {
                        Toast.makeText(mContext, "激活了", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "拒绝了", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void showAsrNlpText(final String nlp, final String action) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mNLPTv != null) {
                        mNLPTv.setText("NLP：" + nlp);
                    }
                    if (mActionTv != null) {
                        mActionTv.setText("Action：" + action);
                    }
                }
            });
        }

        @Override
        public void showRecognizeError(final String errorType) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mErrorTv != null) {
                        mErrorTv.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        mErrorTv.setText("errorType：" + errorType);

                        mErrorTv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mErrorTv != null) {
                                    mErrorTv.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }, 2000);
                    }
                }
            });
        }
    };

    private ServerConfig getServiceConfig() {
        // "workdir_asr_cn": 算法配置文件在assets目录中的位置
        // "ttc": 算法库配置文件名 eg: device.ttc.cfg -> "ttc"
        // useOtherAudio: 是否使用用户自己的audio数据模块
        ServerConfig config = new ServerConfig("workdir_asr_cn", "ttc", false);
        // 设置日志配置
        config.setLogConfig(Logger.LEVEL_D, true, true);
        // key: Rokid开放平台注册时生成的Key
        String key = "BBF450D04CC14DBD88E960CF5D4DD697";
        // secret: Rokid开放平台注册时生成的Secret
        String secret = "29F84556B84441FC885300CD6A85CA70";
        // deviceTypeId: Rokid开放平台注册时生成的DeviceTypeId
        String deviceTypeId = "3301A6600C6D44ADA27A5E58F5838E02";
        // deviceId: 设备SN号，由6~15位的字母和数字组成，不能含有空格和特殊符号
        String deviceId = "57E741770A1241CP";
        String ignoreMoveConfig = "false";

        // 测试配置使用
        Map<String, String> paramMap = readConfigFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CONFIG_FILE_NAME);
        if (paramMap != null) {
            if (!TextUtils.isEmpty(paramMap.get("key"))){
                key = paramMap.get("key");
            }
            if (!TextUtils.isEmpty(paramMap.get("secret"))){
                secret = paramMap.get("secret");
            }
            if (!TextUtils.isEmpty(paramMap.get("deviceTypeId"))){
                deviceTypeId = paramMap.get("deviceTypeId");
            }
            if (!TextUtils.isEmpty(paramMap.get("deviceId"))){
                deviceId = paramMap.get("deviceId");
            }
            if (!TextUtils.isEmpty(paramMap.get("ignoreMoveConfig"))){
                ignoreMoveConfig = paramMap.get("ignoreMoveConfig");
            }
        }
        config.setKey(key).setSecret(secret).setDeviceTypeId(deviceTypeId).setDeviceId(deviceId);
        if ("true".equals(ignoreMoveConfig)) {
            // 忽略移动文件
            config.setIgnoreMoveConfig(true);
        }
        if (mIgnoreSuppressAudioVolume) {
            config.setIgnoreSuppressAudioVolume(true);
        }

        return config;
    }

    /**
     * 获取配置文件信息
     *
     * @param path 文件目录
     * @return
     */
    private Map<String, String> readConfigFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        HashMap<String, String> paramMap = new HashMap<>();
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(path);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line = "";
                String[] arrs = null;
                while ((line=br.readLine())!=null) {
                    arrs = line.split("=");
                    if (arrs.length == 2 && !TextUtils.isEmpty(arrs[0])) {
                        if (arrs[0] != null) {
                            arrs[0] = arrs[0].trim();
                        }
                        if (arrs[1] != null) {
                            arrs[1] = arrs[1].trim();
                        }
                        paramMap.put(arrs[0].trim(), arrs[1].trim());
                        Logger.w(TAG, "属性：" + arrs[0] + " = " + arrs[1] );
                    }
                }
                br.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return paramMap;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.start_play_tts_btn:
                Logger.d(TAG, "onClick(): stop_play_media_btn");
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.playTtsVoice("大家好，我是最牛的电视小精灵，若琪！");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.start_play_media_btn:
                Logger.d(TAG, "onClick(): start_play_media_btn");
                mAsrControlPresenter.startMediaPlay();
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.setUserMediaPlaying(true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stop_play_media_btn:
                Logger.d(TAG, "onClick(): stop_play_media_btn");
                mAsrControlPresenter.stopMediaPlay();
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.setUserMediaPlaying(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public void requestPermission() {
        PerssionManager.requestPerrion(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PerssionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mContext = null;
        if (mPcmClientManager != null) {
            mPcmClientManager.onDestroy();
            mPcmClientManager = null;
        }
        mAsrUiView = null;

        mAsrControlPresenter.releaseMediaPlay();
        mAsrControlPresenter = null;
        Logger.d(TAG, "onDestroy(): is called");

        mHander.removeCallbacksAndMessages(this);
        mHander = null;
        unbindService(mAiServiceConnection);

        mServiceIntent = null;

        super.onDestroy();
    }
}

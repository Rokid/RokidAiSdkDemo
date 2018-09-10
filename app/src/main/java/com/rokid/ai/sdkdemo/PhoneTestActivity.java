package com.rokid.ai.sdkdemo;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.ai.audioai.afe.RokidAFEProxy;
import com.rokid.ai.audioai.aidl.IRokidAudioAiListener;
import com.rokid.ai.audioai.aidl.ServerConfig;
import com.rokid.ai.audioai.util.FileUtil;
import com.rokid.ai.audioai.util.Logger;
import com.rokid.ai.sdkdemo.util.PerssionManager;

import java.util.UUID;

public class PhoneTestActivity extends Activity {


    private RokidAFEProxy mRokidAFEProxy;
    private static final String TAG = PhoneTestActivity.class.getSimpleName();
    private static final int FREQUENCY = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNELDOUBLE = AudioFormat.CHANNEL_IN_STEREO;
    private static final int ENCODING_BIT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private Context mContext;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_test);

        requestPermission();
        mTextView =  findViewById(R.id.tv_show_asr_audio_phone_test);
        mContext = this;
    }


    private IRokidAudioAiListener mAudioAiListener = new IRokidAudioAiListener.Stub() {

        private String mListenerKey = FileUtil.getStringID();

        @Override
        public void onIntermediateSlice(int id, String asr) throws RemoteException {
            String s = "onIntermediateSlice(): asr = " + asr;
            Logger.d(TAG, s);

        }

        @Override
        public void onIntermediateEntire(int id, String asr) throws RemoteException {

            final String mTemp = asr;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(mTemp);
                }
            });

            Logger.d(TAG, "onIntermediateEntire(): asr = " + asr);
        }

        @Override
        public void onCompleteNlp(int id, String nlp, String action) throws RemoteException {

            String s = "onCompleteNlp(): nlp = " + nlp + " action = " + action + "\n\r";
            Logger.d(TAG, s);

        }

        @Override
        public void onVoiceEvent(int id, int event, float sl, float energy) throws RemoteException {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "激活了", Toast.LENGTH_SHORT).show();
                }
            });
            String s = "onVoiceEvent(): event = " + event + ", sl = " + sl + ", energy = " + energy + "\n\r";
            Logger.d(TAG, s);
        }

        @Override
        public void onRecognizeError(int id, int errorCode) throws RemoteException {

            String s = "onRecognizeError(): errorCode = " + errorCode + "\n\r";
            Logger.d(TAG, s);

        }

        @Override
        public void onServerSocketCreate(String ip, int post) throws RemoteException {

        }


        @Override
        public void onPcmServerPrepared() throws RemoteException {

        }

        @Override
        public String getKey() throws RemoteException {
            return mListenerKey;
        }


        @Override
        public void controlNlpAppExit() throws RemoteException {
            Logger.d(TAG,"controlNlpAppExit(): called");
        }

        @Override
        public boolean interceptCloudNlpControl(int id, String nlp, String action) throws RemoteException {
            Logger.d(TAG,"interceptCloudNlpControl(): called");
            return false;
        }

    };


    private void startAfeServer(ServerConfig config) {
        boolean commandInit = false;
        if (config != null) {
            commandInit = config.isMustInit();
        }
        if (mRokidAFEProxy != null) {
            if (mRokidAFEProxy.isServiceRunning() && !commandInit) {
                return;
            }
            mRokidAFEProxy.startAfeServer(config);
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

    private void prepareAfe(int status) {
        // 初始化算法处理中心

        mRokidAFEProxy = new RokidAFEProxy(this);
        ServerConfig serverConfig = null;
        if(status == 0) {
            serverConfig = new ServerConfig("workdir_asr_cn","phonetest", false);
        } else if(status == 1) {
            serverConfig = new ServerConfig("workdir_asr_cn","phonetest2", false);
        }
        mRokidAFEProxy.setCurrentConfig(serverConfig);
        mRokidAFEProxy.addResultListener(mAudioAiListener);

        String strTemp = UUID.randomUUID().toString().replace("-", "");

        serverConfig.setMustInit(true);
        serverConfig.setKey("BBF450D04CC14DBD88E960CF5D4DD697");
        serverConfig.setSecret("29F84556B84441FC885300CD6A85CA70");
        serverConfig.setDeviceTypeId("3301A6600C6D44ADA27A5E58F5838E02");
        serverConfig.setDeviceId(strTemp);
        startAfeServer(serverConfig);
    }

    private void prepareAudioRecord(final int status) {

        final int statusMic = status;
        new Thread(new Runnable() {
            @Override
            public void run() {

                int bufferSize = 0;
                if(statusMic == 0) {
                    bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, AudioFormat.ENCODING_PCM_16BIT);
                } else if(statusMic == 1) {
                    bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNELDOUBLE, AudioFormat.ENCODING_PCM_16BIT);
                }
                if(mAudioRecord == null) {

                    if(statusMic == 0) {
                        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                                , FREQUENCY
                                , CHANNEL
                                , ENCODING_BIT
                                , bufferSize);
                    } else if(statusMic == 1) {
                        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                                , FREQUENCY
                                , CHANNELDOUBLE
                                , ENCODING_BIT
                                , bufferSize);
                    }

                    boolean isRecording = true;
                    int length = 2560;
                    byte[] buffer = new byte[length];
                    mAudioRecord.startRecording();
                    Log.d(TAG, "the pcm data is" + bufferSize);
                    int bufferReadResult;
                    while (isRecording) {
                        bufferReadResult = mAudioRecord.read(buffer, 0, length);
                       // FileWriteReadSdcard.writeToAudiojavaoutFile(buffer);
                        mRokidAFEProxy.addPcmData(bufferReadResult, buffer);
                    }

                }

            }
        }).start();

    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_start_server_phone_test:
                prepareAfe(0);
                break;
            case R.id.btn_start_audio_phone_test:
                prepareAfe(0);
                prepareAudioRecord(0);
                break;
            case R.id.btn_double_start_audio_test:
                prepareAfe(1);
                prepareAudioRecord(1);
                break;
        }
    }

    @Override
    protected void onDestroy() {

        mRokidAFEProxy.onDestroy();
        finish();
        super.onDestroy();
    }
}

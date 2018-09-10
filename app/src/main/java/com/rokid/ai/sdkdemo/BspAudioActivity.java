package com.rokid.ai.sdkdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.ai.audioai.AudioAiConfig;
import com.rokid.ai.audioai.aidl.IRokidAudioAiListener;
import com.rokid.ai.audioai.aidl.IRokidAudioAiService;
import com.rokid.ai.audioai.aidl.ServerConfig;
import com.rokid.ai.audioai.util.FileUtil;
import com.rokid.ai.audioai.util.Logger;
import com.rokid.ai.sdkdemo.presenter.AsrControlPresenter;
import com.rokid.ai.sdkdemo.presenter.AsrControlPresenterImpl;
import com.rokid.ai.sdkdemo.util.PerssionManager;
import com.rokid.ai.sdkdemo.view.IAsrUiView;
import com.rokid.ai.sdkdemo.view.RokidToast;
import com.rokid.aisdk.socket.base.ClientSocketManager;
import com.rokid.aisdk.socket.business.preprocess.IReceiverPcmListener;
import com.rokid.aisdk.socket.business.preprocess.PcmClientManager;
import com.rokid.aisdk.socket.business.record.RecordClientManager;

public class BspAudioActivity extends AppCompatActivity {

    private IRokidAudioAiService mAudioAiService;
    private final static String CONFIG_FILE_NAME = "Rokid_Ai_SDK_Config.txt";
    public static final String IGNORE_SUPPRESS_AUDIO_VOLUME = "Ignore_Suppress_Audio_Volume";
    private static final String TAG = BspAudioActivity.class.getSimpleName();

    private static final int FREQUENCY = 48000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNELDOUBLE = AudioFormat.CHANNEL_IN_STEREO;
    private static final int ENCODING_BIT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private boolean mIgnoreSuppressAudioVolume = false;
    private AsrControlPresenter mAsrControlPresenter;
    private Context mContext = null;
    private TextView mAsrStateTV;
    private TextView mAsrResultTv;
    private TextView mNLPTv;
    private TextView mActionTv;
    private TextView mActivationTv;
    private TextView mErrorTv;
    private TextView mPcmTv;
    private int mActivationCount;
    private long mPcmCount;
    private int mBadPcmCount;
    private boolean mCanSendPcm;
    private boolean isRecording;
    private boolean isBindService;
    private RecordClientManager mRecordClientManager;
    private PcmClientManager mPcmSocketManager;
    private Intent mServiceIntent;
    private RokidToast AsrToast;
    private Drawable btn_style;
    private boolean AiServerIsStarted = false;

    /* IRokidAudioAiListener
    * 负责 监听SDK事件
    * */
    private IRokidAudioAiListener mAudioAiListener = new IRokidAudioAiListener.Stub() {


        private String mListenerKey = FileUtil.getStringID();

        @Override
        public void onIntermediateSlice(int id, String asr) throws RemoteException {
            String s = "onIntermediateSlice(): asr = " + asr;
            Logger.d(TAG, s);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrResultText(id, asr, false);
            }

        }

        @Override
        public void onIntermediateEntire(int id, String asr) throws RemoteException {

            final String mTemp = asr;

            Logger.d(TAG, "onIntermediateEntire(): asr = " + asr);

            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrResultText(id, asr, true);
            }
        }

        @Override
        public void onCompleteNlp(int id, String nlp, String action) throws RemoteException {

            String s = "onCompleteNlp(): nlp = " + nlp + " action = " + action + "\n\r";
            Logger.d(TAG, s);

            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrNlpText(id, nlp, action);
            }

        }

        @Override
        public void onVoiceEvent(int id, int event, float sl, float energy) throws RemoteException {


            String s = "onVoiceEvent(): event = " + event + ", sl = " + sl + ", energy = " + energy + "\n\r";
            Logger.d(TAG, s);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showAsrEvent(id, event, sl, energy);
            }
        }

        @Override
        public void onRecognizeError(int id, int errorCode) throws RemoteException {

            String s = "onRecognizeError(): errorCode = " + errorCode + "\n\r";
            Logger.d(TAG, s);
            if (mAsrControlPresenter != null) {
                mAsrControlPresenter.showRecognizeError(errorCode);
            }

        }

        @Override
        public void onServerSocketCreate(String ip, int port) throws RemoteException {

            Logger.d(TAG,"onServerSocketCreate(): ip = " + ip + ", port = " + port);
            if (mRecordClientManager != null) {
                mRecordClientManager.startSocket(ip, port, mConnnectListener);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    //int bufferSize = 16896;
                    int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNELDOUBLE, AudioFormat.ENCODING_PCM_16BIT);
                    if(mAudioRecord == null) {
                        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                                , FREQUENCY
                                , CHANNELDOUBLE
                                , ENCODING_BIT
                                , bufferSize);

                        isRecording = true;
                        byte[] buffer = new byte[bufferSize];
                        mAudioRecord.startRecording();
                        Logger.d(TAG, "the pcm data is" + bufferSize);

                        int bufferReadResult;
                        while (isRecording) {
                            bufferReadResult = mAudioRecord.read(buffer, 0, bufferSize);
                            if (mCanSendPcm) {
//                                Logger.d(TAG, "sendRecordData data is" + bufferSize);
                                if (mRecordClientManager != null) {
                                    mRecordClientManager.sendRecordData(buffer);
                                }
                            }
                        }

                    }

                }
            }).start();
        }

        @Override
        public void onPcmServerPrepared() throws RemoteException {
            Logger.d(TAG,"onPcmServerPrepared(): called");
            if (mPcmSocketManager != null) {
                mPcmSocketManager.startSocket(null, mPcmReceiver, 30003);
            }
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

    private ServiceConnection mAiServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                isBindService = true;
                Logger.d(TAG, "the onServiceConenct is called");
                if (service != null) {
                    Logger.d(TAG, "the onServiceConenct is called111");
                    mAudioAiService = IRokidAudioAiService.Stub.asInterface(service);
                    try {
                        Logger.d(TAG, "the onServiceConenct is called222");
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

    private ClientSocketManager.IConnnectListener mConnnectListener = new ClientSocketManager.IConnnectListener() {
        @Override
        public void onConnectSuccess(ClientSocketManager socketManager) {
            mCanSendPcm = true;
            Logger.d(TAG,"IConnnectListener(): onConnectSuccess");

        }

        @Override
        public void onConnectFailed(ClientSocketManager socketManager) {
            mCanSendPcm = false;
            Logger.d(TAG,"IConnnectListener(): onConnectFailed");
        }
    };

    private IReceiverPcmListener mPcmReceiver = new IReceiverPcmListener() {
        @Override
        public void onPcmReceive(int length, byte[] data) {
            String s = "onPcmReceive(): len = " + length + "\n\r";
            showPcmData(length, data);
        }
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
    };

    private IAsrUiView mAsrUiView = new IAsrUiView() {

        @Override
        public void showAsrResultText(final String str, final boolean isFinish) {
            Logger.d(TAG, "onVoiceEvent(): showAsrResultText = " + str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAsrResultTv != null) {
                        mAsrResultTv.setText(str);
                        AsrToast.setText(str);
                        AsrToast.show();
                    }
                }
            });
        }

        @Override
        public void showAsrStateText(final String str) {
            Logger.d(TAG, "onVoiceEvent(): event = " + str);
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
            Logger.d(TAG, "onVoiceEvent(): event = " + isActivation);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActivation) {
                        mActivationCount = mActivationCount + 1;
                        if (mActivationTv != null) {
                            mActivationTv.setText("激活访问次数：" + mActivationCount);
                        }
                        //Toast.makeText(mContext, "激活了", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(mContext, "拒绝了", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void showAsrNlpText(final String nlp, final String action) {
            Logger.d(TAG, "onVoiceEvent(): event = " + nlp + "action is" + action);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mNLPTv != null) {
                        mNLPTv.setText(nlp);
                    }
                    if (mActionTv != null) {
                        mActionTv.setText(action);
                    }
                }
            });
        }

        @Override
        public void showRecognizeError(final String errorType) {
            Logger.d(TAG, "onVoiceEvent(): event = " + errorType);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mErrorTv != null) {
                        mErrorTv.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        mErrorTv.setText(errorType);

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
        Spinner  config_select =   findViewById(R.id.config_select);
        ServerConfig config = new ServerConfig(
                "workdir_asr_cn", config_select.getSelectedItem().toString(), true);

        config.setLogConfig(Logger.LEVEL_D, true, true);
        String key = "9EFA575F837E4716A0F276C9D04EDAFE";
        String secret = "DD3A0EB531714DF6B8881B0C012DD044";
        String deviceTypeId = "F36F83B22BA74F9FA2493804BF5A57DA";
        String deviceId = "EF892007621C44A1AC35101C8BDE85D3";
        String ignoreMoveConfig = "false";

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

    public void onClick(View view) {

        Logger.d(TAG, "the Phone AudioNewSDKActivity called");
        switch (view.getId()) {
            case R.id.btn_other_pickup_open:
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.setPickUp(true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_other_pickup_close:
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.setPickUp(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_other_set_angle:
                try {
                    if (mAudioAiService != null) {
                        mAudioAiService.setAngle(100);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_server_start_stop:
                if(!AiServerIsStarted) {
                    mServiceIntent = AudioAiConfig.getIndependentIntent(this);
                    mServiceIntent.putExtra(AudioAiConfig.PARAM_SERVICE_START_CONFIG, getServiceConfig());
                    startService(mServiceIntent);
                    bindService(mServiceIntent, mAiServiceConnection, BIND_AUTO_CREATE);
                    AiServerIsStarted = true;

                    Button btn = (Button) findViewById(R.id.btn_server_start_stop);
                    if(btn_style == null)
                        btn_style = btn.getBackground();
                    btn.setBackgroundColor(0xFFFF0000);
                    btn.setText("停止服务");
                }else {
                    unbindService(mAiServiceConnection);
                    stopService(mServiceIntent);

                    AiServerIsStarted = false;
                    Button btn = (Button) findViewById(R.id.btn_server_start_stop);
                    btn.setText("启动服务");
                    btn.setBackgroundDrawable(btn_style);
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bsp_audio);
        mContext = this;
        mAsrStateTV = findViewById(R.id.main_ast_state_tv);
        mAsrResultTv = findViewById(R.id.main_ast_result_tv);
        mNLPTv = findViewById(R.id.main_ast_npl_tv);
        mActionTv = findViewById(R.id.main_ast_action_tv);
        mActivationTv = findViewById(R.id.main_ast_activation_tv);
        mErrorTv = findViewById(R.id.main_ast_error_tv);
        mPcmTv = findViewById(R.id.main_ast_pcm_tv);
        PerssionManager.requestPerrion(this);
        mRecordClientManager = new RecordClientManager();
        mPcmSocketManager = new PcmClientManager();
        mAsrControlPresenter = new AsrControlPresenterImpl(mContext, mAsrUiView);
        AsrToast = RokidToast.makeText(mContext, "这里显示ASR结果", Toast.LENGTH_LONG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PerssionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        isRecording = false;
        mCanSendPcm = false;
        mContext = null;

        mAsrUiView = null;

        if (mAsrControlPresenter != null) {
            mAsrControlPresenter.releaseMediaPlay();
            mAsrControlPresenter = null;
        }

        Logger.d(TAG, "onDestroy(): is called");
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }

        try {
            if (isBindService) {
                unbindService(mAiServiceConnection);
            }
//            stopService(new Intent(mContext, AudioAiService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPcmSocketManager.onDestroy();
        mPcmSocketManager = null;

        mRecordClientManager.onDestroy();
        mRecordClientManager = null;

        super.onDestroy();
    }
}

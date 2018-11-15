package com.rokid.ai.sdkdemo.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rokid.ai.basic.AudioAiConfig;
import com.rokid.ai.basic.aidl.IRokidAudioAiListener;
import com.rokid.ai.basic.aidl.IRokidAudioAiService;
import com.rokid.ai.basic.aidl.ServerConfig;
import com.rokid.ai.basic.util.FileUtil;
import com.rokid.ai.basic.util.Logger;

/**
 * Func: 前台服务
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/6/28
 */
public class TipsService extends Service {


    private static final String TAG = "TipsService";

    private Intent mServiceIntent;
    private boolean mServerRunning;
    private IRokidAudioAiService mAudioAiService;


    @Override
    public void onCreate() {
        super.onCreate();
        mServerRunning = false;
    }

    private Handler mToastHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand(): ****** ");
        try {
            if (intent != null) {
                ServerConfig config = intent.getParcelableExtra(AudioAiConfig.PARAM_SERVICE_START_CONFIG);
                if (config != null) {
                    if (!mServerRunning) {
                        Logger.d(TAG, "onStartCommand(): run ");
                        if (startAiServer(config)) {
                            Logger.d(TAG, "onStartCommand(): registListener ");
                            registListener();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);
        return Service.START_REDELIVER_INTENT;
    }

    public boolean startAiServer(ServerConfig config) {
        try {
            mServiceIntent = AudioAiConfig.getIndependentIntent(getApplicationContext());
            mServiceIntent.putExtra(AudioAiConfig.PARAM_SERVICE_START_CONFIG, config);
            startService(mServiceIntent);
            mServerRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mServerRunning;
    }


    private ServiceConnection mAiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                mAudioAiService = IRokidAudioAiService.Stub.asInterface(service);
                try {
                    service.linkToDeath(mDeathRecipient, 0);
                    mAudioAiService.registAudioAiListener(mAudioAiListener);
                    Logger.d(TAG, "mAiServiceConnection(): registListener ");
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

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            try {
                Logger.d(TAG, "DeathRecipient(): binderDied start");
                startService(mServiceIntent);
                mServerRunning = true;
                registListener();
                Logger.d(TAG, "DeathRecipient(): binderDied end");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private IRokidAudioAiListener mAudioAiListener = new IRokidAudioAiListener.Stub() {

        private String mListenerKey = FileUtil.getStringID();

        @Override
        public void onIntermediateSlice(int id, String asr) {
            Logger.d(TAG, "onIntermediateSlice(): id = " + id + ", asr = " + asr);

        }

        @Override
        public void onIntermediateEntire(int id, final String asr) {
            Logger.d(TAG, "onIntermediateEntire(): id = " + id + ", asr = " + asr);
            mToastHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"ASR: " + asr, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCompleteNlp(int id, String nlp, String action) {
            Logger.d(TAG, "onCompleteNlp(): id = " + id + ", nlp = " + nlp + "\n action = " + action);

        }

        @Override
        public void onVoiceEvent(int id, int event, float sl, float energy) {
//            Logger.d(TAG, "onVoiceEvent Thread：" + Thread.currentThread().getName());
            Logger.d(TAG, "onVoiceEvent(): id = " + id + ", event = " + event + ", sl = " + sl + ", energy = " + energy);

        }

        @Override
        public void onRecognizeError(int id, int errorCode) {
            Logger.d(TAG, "onRecognizeError(): id = " + id + ", errorCode = " + errorCode);

        }

        @Override
        public void onServerSocketCreate(String ip, int post) {

        }

        @Override
        public void onPcmServerPrepared() {

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

    public void registListener() {
        bindService(mServiceIntent, mAiServiceConnection, BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy(): ");
        mServerRunning = false;

        try {
            if (mAudioAiService != null) {
                mAudioAiService.asBinder().unlinkToDeath(mDeathRecipient, 0);
                mAudioAiService = null;
            }
        } catch (Throwable e) {
        }

        if (mToastHandler != null) {
            mToastHandler.removeCallbacksAndMessages(null);
            mToastHandler = null;
        }
        try {
            unbindService(mAiServiceConnection);
            Logger.d(TAG, "onDestroy(): unbindService");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stopService(mServiceIntent);
            Logger.d(TAG, "onDestroy(): stopService");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}

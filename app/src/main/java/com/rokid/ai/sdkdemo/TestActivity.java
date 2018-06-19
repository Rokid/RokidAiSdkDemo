package com.rokid.ai.sdkdemo;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rokid.ai.audioai.afe.AfeParam;
import com.rokid.ai.audioai.afe.RokidAFEProxy;
import com.rokid.ai.audioai.aidl.IRokidAudioAiListener;
import com.rokid.ai.audioai.aidl.ServerConfig;
import com.rokid.ai.audioai.util.Logger;
import com.rokid.ai.sdkdemo.util.PerssionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author siokagami
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnInitService;
    private Button btnTestStart;
    private Button btnInitTestFile;
    private TextView tvTestStatus;
    private TextView tvSuccess;


    private TextView tvFileList;

    private File[] testFileList;

    private static final String TAG = "AudioAiService";

    private static int MESSAGE_TEST = 1001;

    private int testFileIndex = 0;

    private int successNum = 0;

    private RokidAFEProxy mRokidAFEProxy;

    private static String TEST_FOLDER = Environment.getExternalStorageDirectory() + "/" + "testFile";

    private IRokidAudioAiListener mAudioAiListener = new IRokidAudioAiListener.Stub() {

        @Override
        public void onIntermediateSlice(String asr) throws RemoteException {
            String s = "onIntermediateSlice(): asr = " + asr;
            Logger.d(TAG, s);

        }

        @Override
        public void onIntermediateEntire(String asr) throws RemoteException {
            Logger.d(TAG, "onIntermediateEntire(): asr = " + asr);
        }

        @Override
        public void onCompleteNlp(String nlp, String action) throws RemoteException {
            successNum++;
            String s = "onCompleteNlp(): nlp = " + nlp + " action = " + action + "\n\r";
            Logger.d(TAG, s);
            appendStringToFile(s, TEST_FOLDER, "testLog.txt");
            appendStringToFile(successNum + "/" + testFileList.length, TEST_FOLDER, "testLog.txt");
            appendStringToFile("===========================================", TEST_FOLDER, "testLog.txt");

        }

        @Override
        public void onVoiceEvent(int event, float sl, float energy) throws RemoteException {
            String s = "onVoiceEvent(): event = " + event + ", sl = " + sl + ", energy = " + energy + "\n\r";
            Logger.d(TAG, s);
            appendStringToFile(s, TEST_FOLDER, "testLog.txt");
        }

        @Override
        public void onRecognizeError(int errorCode) throws RemoteException {
            String s = "onRecognizeError(): errorCode = " + errorCode + "\n\r";
            Logger.d(TAG, s);
            appendStringToFile(s, TEST_FOLDER, "testLog.txt");
            appendStringToFile("===========================================", TEST_FOLDER, "testLog.txt");

        }

        @Override
        public void onServerSocketCreate(String ip, int post) throws RemoteException {

        }

        @Override
        public void onPcmServerPrepared() throws RemoteException {

        }

    };

    private Handler testHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (testFileIndex < testFileList.length) {
                        putPcmData(testFileList[testFileIndex]);
                    }
                }
            }).start();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        requestPermission();
        initView();
    }

    private void initView() {
        btnInitService = findViewById(R.id.btn_init_service);
        btnTestStart = findViewById(R.id.btn_test_start);
        btnInitTestFile = findViewById(R.id.btn_init_test_file);
        tvTestStatus = findViewById(R.id.tv_test_status);
        tvFileList = findViewById(R.id.tv_file_list);
        tvSuccess = findViewById(R.id.tv_success);


        btnInitService.setOnClickListener(this);
        btnInitTestFile.setOnClickListener(this);
        btnTestStart.setOnClickListener(this);
    }

    private void testPrepare() {
        // 初始化算法处理中心
        mRokidAFEProxy = new RokidAFEProxy(this);
        mRokidAFEProxy.addResultListener(new ServerConfig("workdir_asr_cn","ttc", false), mAudioAiListener);

        AfeParam param = new AfeParam();
        param.mustInit = true;
        param.key = "BBF450D04CC14DBD88E960CF5D4DD697";
        param.secret = "29F84556B84441FC885300CD6A85CA70";
        param.deviceTypeId = "3301A6600C6D44ADA27A5E58F5838E02";
        param.deviceId = "57E741770A1241CP";
        startAfeServer(param);
    }

    private int frameSize = 4224;
    private void putPcmData(File pcmFile) {
        FileInputStream mFIleInput = null;
        try {

            mFIleInput = new FileInputStream(pcmFile);
            byte[] myte = new byte[frameSize];
            int count = 0;
            while ((count = mFIleInput.read(myte)) > 0) {

                if (count != frameSize) {
                    Log.d("count", "the count is error not 16128");
                }
                if (count == frameSize) {
                    Log.d(TAG, "the frameSzie is " + frameSize);
                    mRokidAFEProxy.addPcmData(count, myte);
                    Thread.sleep(1000);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testFileIndex++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTestStatus.setText(getResources().getString(R.string.test_status, testFileList.length, testFileIndex + 1, successNum));
            }
        });
        if (testFileIndex < testFileList.length) {
            testHandler.sendEmptyMessage(MESSAGE_TEST);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvSuccess.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    private void startAfeServer(AfeParam param) {
        boolean commandInit = false;
        if (param != null) {
            commandInit = param.mustInit;
        }
        if (mRokidAFEProxy != null) {
            if (mRokidAFEProxy.isServiceRunning() && !commandInit) {
                return;
            }
            mRokidAFEProxy.startAfeServer(param);
        }
    }

    public void appendStringToFile(String msg, String fileDirPath, String fileName) {
        if (TextUtils.isEmpty(fileDirPath)) {
            return;
        }
        if (!isFileExists(fileDirPath)) {
            new File(fileDirPath).mkdirs();
        }
        FileWriter writer;
        try {
            writer = new FileWriter(Environment.getExternalStorageDirectory() + "/" + fileName, true);
            writer.write(msg);
            writer.write("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_init_service:
                testPrepare();
                break;
            case R.id.btn_test_start:
                testHandler.sendEmptyMessage(MESSAGE_TEST);
                tvTestStatus.setText(getResources().getString(R.string.test_status, testFileList.length, testFileIndex + 1, successNum));
                break;
            case R.id.btn_init_test_file:
                File folder = new File(TEST_FOLDER);
                testFileList = folder.listFiles();
                StringBuilder s = new StringBuilder();
                for (File f : testFileList) {
                    s.append(f.getAbsolutePath()).append("\n\r");
                }
                tvFileList.setText(s);
                break;
            case R.id.btn_angle_activity_test:

                mRokidAFEProxy.setAngle(89);
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



}

package com.rokid.ai.sdkdemo.presenter;

import android.content.Context;
import com.rokid.ai.sdkdemo.model.MediaPlayManagerImpl;
import com.rokid.ai.sdkdemo.model.IMediaPlayManager;
import com.rokid.ai.sdkdemo.util.LimitQueue;
import com.rokid.ai.sdkdemo.view.IAsrUiView;
import com.rokid.voicerec.VoiceRecognize;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Func：presenter实现 -> 用户控制
 */
public class AsrControlPresenterImpl implements AsrControlPresenter {

    private IMediaPlayManager mPlayManager;
    private IAsrUiView mAsrUiView;
    private LimitQueue<String> mEventQueue;
    private LimitQueue<String> mResultQueue;
    private SimpleDateFormat mDateFormatter;
    private SimpleDateFormat mTimeFormatter;

    public AsrControlPresenterImpl(Context context, IAsrUiView uiView) {
        this.mAsrUiView = uiView;
        this.mPlayManager = new MediaPlayManagerImpl(context);
        mEventQueue = new LimitQueue<>(5);
        mResultQueue = new LimitQueue<>(5);
        mDateFormatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss SSS");
        mTimeFormatter = new SimpleDateFormat ("HH:mm:ss SSS");
    }

    @Override
    public void startMediaPlay() {
        if (mPlayManager != null) {
            mPlayManager.startPlayMedia();
        }
    }

    @Override
    public void stopMediaPlay() {
        if (mPlayManager != null) {
            mPlayManager.stopPlayMedia();
        }
    }

    @Override
    public void releaseMediaPlay() {
        if (mPlayManager != null) {
            mPlayManager.releaseMedia();
        }
    }

    @Override
    public void showAsrResultText(String str, boolean isFinish) {


        StringBuilder builder = new StringBuilder();
        builder.append("时间: ");
        builder.append(mTimeFormatter.format(new Date()) + "    ");
        if (isFinish) {
            builder.append("最终结果: ");
        } else {
            builder.append("中间结果: ");
        }
        builder.append(str + "    ");

        mResultQueue.offer(builder.toString());
        if (mAsrUiView != null) {
            StringBuilder out = new StringBuilder();
            int len = mResultQueue.size();
            for (int i = 0; i < len; i++) {
                out.append(mResultQueue.get(i) + "\n");
            }
            mAsrUiView.showAsrResultText(out.toString());
        }
    }

    @Override
    public void showAsrEvent(int event, float sl, float energy) {
        try {
            VoiceRecognize.Event eventEnum = VoiceRecognize.Event.values()[event];

            String eventType = eventEnum.name();
            StringBuilder builder = new StringBuilder();
            builder.append("时间: ");
            builder.append(mDateFormatter.format(new Date()) + "    ");
            builder.append("事件类型: ");
            builder.append(eventType + "    ");
            builder.append("唤醒角度: ");
            builder.append(sl + "   ");
            builder.append("说话能量: ");
            builder.append(energy);

            mEventQueue.offer(builder.toString());

            if (mAsrUiView != null) {
                StringBuilder out = new StringBuilder();
                int len = mEventQueue.size();
                for (int i = 0; i < len; i++) {
                    out.append(mEventQueue.get(i) + "\n");
                }
                mAsrUiView.showAsrStateText(out.toString());
            }

            switch (eventEnum) {
                case EVENT_VOICE_ACCEPT:
                    showAsrActivation(true);
                    break;
                case EVENT_VOICE_REJECT:
                    showAsrActivation(false);
                    break;
                    default:
                        break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showRecognizeError(int errorCode) {
        try {
            String errorType = VoiceRecognize.ExceptionCode.values()[errorCode].name();
            if (mAsrUiView != null) {
                mAsrUiView.showRecognizeError(errorType);
                mAsrUiView.showAsrNlpText("", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAsrActivation(boolean isActivation) {
        if (mAsrUiView != null) {
            mAsrUiView.showAsrActivation(isActivation);
        }
    }

    @Override
    public void showAsrNlpText(String nlp, String action) {
        if (mAsrUiView != null) {
            mAsrUiView.showAsrNlpText(nlp, action);
        }
    }
}

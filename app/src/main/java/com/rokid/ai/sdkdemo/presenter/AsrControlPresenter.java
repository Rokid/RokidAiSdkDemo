package com.rokid.ai.sdkdemo.presenter;

/**
 * Func：presenter接口 -> 用户控制
 */
public interface AsrControlPresenter {

    /**
     * 开始播放音乐
     */
    void startMediaPlay();

    /**
     * 停止播放音乐
     */
    void stopMediaPlay();

    /**
     * 回收音乐播放资源
     */
    void releaseMediaPlay();

    /**
     * 展示ASR处理结果返回信息
     *
     * @param id  会话ID
     * @param str 数据信息
     */
    void showAsrResultText(int id, String str, boolean isFinish);

    /**
     * 展示ASR事件信息
     *
     * @param id  会话ID
     * @param event 语音事件类型VoiceRecognize.Event的ordinal()值
     * @param sl 当前唤醒角度(0到360度之间)
     * @param energy 当前说话能量值(0到1之间的浮点数)
     */
    void showAsrEvent(int id, int event, float sl, float energy);

    /**
     * 展示ASR状态信息
     *
     * @param errorCode 错误码 VoiceRecognize.ExceptionCode
     */
    void showRecognizeError(int errorCode);

    /**
     * 展示NLP数据
     *
     * @param id  会话ID
     * @param nlp 自然语义解析结果
     * @param action 云端skill结果
     */
    void showAsrNlpText(int id, String nlp, String action);
}
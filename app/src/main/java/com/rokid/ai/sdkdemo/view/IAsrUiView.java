package com.rokid.ai.sdkdemo.view;

/**
 * Func：view -> 用户控制
 */
public interface IAsrUiView {
    /**
     * 设置ASR处理结果返回信息的展示
     *
     * @param str 数据信息
     */
    void showAsrResultText(String str);

    /**
     * 设置ASR状态信息的展示
     *
     * @param str 状态信息
     */
    void showAsrStateText(String str);

    /**
     * 设置激活成功的展示
     *
     * @param isActivation ture: 激活成功 false：激活失败
     */
    void showAsrActivation(boolean isActivation);

    /**
     * 设置NLP数据的展示
     *
     * @param nlp 自然语义解析结果
     * @param action 云端skill结果
     */
    void showAsrNlpText(String nlp, String action);

    /**
     * 设置ASR错误信息的展示
     */
    void showRecognizeError(String errorType);
}

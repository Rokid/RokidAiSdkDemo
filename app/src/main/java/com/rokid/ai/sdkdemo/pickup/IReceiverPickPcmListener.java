package com.rokid.ai.sdkdemo.pickup;

/**
 * Func: 算法  激活、拾音 pcm数据获取后处理监听回调
 * SpeechExecutor、 RokidAiSDK中接收turen 30018端口pcm数据使用
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/1
 */
public interface IReceiverPickPcmListener {

    /**
     * 激活拾音pcm数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     */
    void onPcmReceive(int length, byte[] data);

    /**
     * 单纯激活词语音数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     * @param length 整个激活词长度
     */
    void onActivationReceive(int length, byte[] data, int totalLength);

    /**
     * 单纯拾音语音数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     */
    void onPickupReceive(int length, byte[] data);
}

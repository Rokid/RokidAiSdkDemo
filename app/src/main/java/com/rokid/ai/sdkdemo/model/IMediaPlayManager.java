package com.rokid.ai.sdkdemo.model;

/**
 * Func: model接口 -> 媒体播放控制接口
 */
public interface IMediaPlayManager {

    /**
     * 用户开始播放音频媒体
     */
    void startPlayMedia();

    /**
     * 用户结束播放音频媒体
     */
    void stopPlayMedia();

    /**
     * 用户回收音频媒体资源
     */
    void releaseMedia();
}

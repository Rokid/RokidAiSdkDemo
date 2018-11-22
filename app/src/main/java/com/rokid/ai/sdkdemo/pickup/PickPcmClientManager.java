package com.rokid.ai.sdkdemo.pickup;


import com.rokid.ai.nlpconsumer.util.Logger;
import com.rokid.ai.socket.base.ChannelManager;
import com.rokid.ai.socket.base.ClientSocketManager;

import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Func: 激活、拾音 录音Pcm的Client Socket
 * SpeechExecutor、 RokidAiSDK中接收turen 30018端口pcm数据使用
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/1
 */
public class PickPcmClientManager extends ClientSocketManager {
    private IReceiverPickPcmListener mPcmReceiver;
    private PickPcmMsgControler mPcmMsgControler;


    public PickPcmClientManager() {
        super();
        mPcmMsgControler = new PickPcmMsgControler(mClientReceiver);
    }

    public void setPcmReceiver(IReceiverPickPcmListener pcmReceiver) {
        this.mPcmReceiver = pcmReceiver;
    }

    /**
     * 启动一个socket长连接
     * @param listener    IConnnectListener 连接状态监听
     * @param pcmReceiver IReceiverPickPcmListener 数据处理回调
     * @param port 接收某个端口的数据
     */
    public void startSocket(IConnnectListener listener, IReceiverPickPcmListener pcmReceiver, int port){
        setPcmReceiver(pcmReceiver);
        startSocket("127.0.0.1", port, listener);
    }

    @Override
    protected void onConnectSuccess() {
        super.onConnectSuccess();
        Logger.d("PickPcmClientManager", "onConnectSuccess() ip = " + mSocketIp + ", port = " + mSocketPort);
    }

    @Override
    protected ChannelManager createChannelManager(Socket socket, ChannelManager.IChannelListener listener) {
        ChannelManager channelManager = new PickPcmChannelManager(socket, listener);
        channelManager.addMsgReceiveControler(mPcmMsgControler);
        return channelManager;
    }

    private IReceiverPickPcmListener mClientReceiver = new IReceiverPickPcmListener() {
        @Override
        public void onPcmReceive(int length, byte[] data) {
            if (mPcmReceiver != null) {
                mPcmReceiver.onPcmReceive(length, data);
            }
        }

        @Override
        public void onActivationReceive(int length, byte[] data, int totalLength) {
            if (mPcmReceiver != null) {
                mPcmReceiver.onActivationReceive(length, data, totalLength);
            }
        }

        @Override
        public void onPickupReceive(int length, byte[] data) {
            if (mPcmReceiver != null) {
                mPcmReceiver.onPickupReceive(length, data);
            }
        }
    };

    /**
     * 通过socket发送pcm数据
     *
     * @param data pcm数据
     */
    protected void sendDataBySocket(byte[] data) {
//        Logger.d("PickPcmClientManager", "sendPcmBySocket() len = " + data.length);
        if (data != null && data.length > 0) {
                    // 传输数据
            if (mChannelManager != null && mChannelManager.getDataWriter() != null) {
                synchronized (mChannelManager.getWriteSynchronized()) {
                    try {
                        DataOutputStream writer = mChannelManager.getDataWriter();
                        writer.write(data, 0, data.length);
                        writer.flush();
                        Logger.d("PickPcmClientManager", "writer.flush() len = " + data.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mChannelListener != null) {
                            mChannelListener.onSocketError(mChannelManager);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        mPcmReceiver = null;
        mPcmMsgControler = null;
        mClientReceiver = null;
        mChannelListener = null;
        super.onDestroy();
    }
}

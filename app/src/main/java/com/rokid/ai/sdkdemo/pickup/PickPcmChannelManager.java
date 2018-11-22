package com.rokid.ai.sdkdemo.pickup;


import com.rokid.ai.nlpconsumer.util.Logger;
import com.rokid.ai.socket.base.ChannelManager;
import com.rokid.ai.socket.base.IMsgReceiveControler;

import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Func: 激活、拾音 PCM流 处理
 * SpeechExecutor、 RokidAiSDK中接收turen 30018端口pcm数据使用
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/1
 */
public class PickPcmChannelManager extends ChannelManager {

    private IMsgReceiveControler mPcmControler;

    public PickPcmChannelManager(Socket socket, IChannelListener listener) {
        super(socket, listener);
    }

    @Override
    public Object getReadSynchronized() {
//        Logger.d("PickPcmChannelManager", "onPcmReceive ++++++++ getReadSynchronized() call ");
        return super.getReadSynchronized();
    }

    @Override
    public Object getWriteSynchronized() {
//        Logger.d("PickPcmChannelManager", "onPcmReceive -------- getWriteSynchronized() call ");
        return super.getWriteSynchronized();
    }

    @Override
    protected void controlReadData() throws Exception {
        if (mReceiveListenerList != null && mReceiveListenerList.size() > 0) {
            // 0 为无效消息
            mPcmControler = mReceiveListenerList.get(0);
            if (mPcmControler != null) {

                boolean ret = mPcmControler.onMsgReceive(mDataReader, mDataWriter);
                if (!ret) {
                    throw new Exception("socket is close()");
                }
            }
        }
    }

    @Override
    protected void onStreamPrepared() {
        super.onStreamPrepared();
        sendHeaderData();
    }

    /**
     * 发送录音数据
     */
    private void sendHeaderData(){
        byte[] data = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            data[i] = 0;
        }
        sendDataBySocket(data);
    }

    /**
     * 通过socket发送pcm数据
     *
     * @param data pcm数据
     */
    private void sendDataBySocket(byte[] data) {
//        Logger.d("PickPcmChannelManager", "sendPcmBySocket() len = " + data.length);
        if (data != null && data.length > 0) {
            // 传输数据
            if (getDataWriter() != null) {
                synchronized (getWriteSynchronized()) {
                    try {
                        DataOutputStream writer = getDataWriter();
                        writer.write(data, 0, data.length);
                        writer.flush();
                        Logger.d("PickPcmChannelManager", "writer.flush() len = " + data.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mChannelListener != null) {
                            mChannelListener.onSocketError(this);
                        }
                    }
                }
            }
        }
    }
}

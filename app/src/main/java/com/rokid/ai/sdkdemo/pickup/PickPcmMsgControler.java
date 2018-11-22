package com.rokid.ai.sdkdemo.pickup;

import com.rokid.ai.socket.base.IMsgReceiveControler;
import com.rokid.ai.socket.base.SocketConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Func: Socket 算法激活、拾音 pcm数据接收处理
 * SpeechExecutor、 RokidAiSDK中接收turen 30018端口pcm数据使用
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/1
 */
public class PickPcmMsgControler implements IMsgReceiveControler {

    private static final String TAG = "PickPcmMsgControler";

    private int mDataType;

    private IReceiverPickPcmListener mPcmListener;

    /**
     * 激活词长度
     */
    private int mTotalActivationLength;

    /**
     * 还需继续截取的激活词长度
     */
    private int mNeedActivationLength;

    /**
     * 还需继续截取的激活词起始位置
     */
    private int mNeedActivationStart;

    /**
     * 激活词数据需要不断截取
     */
    private boolean mNeedGetActivation;

    public PickPcmMsgControler(IReceiverPickPcmListener listener) {
        mDataType = SocketConfig.MSG_TYPE_PRE_PROCESS_PCM;
        this.mPcmListener = listener;

//        byte[] head = "HEAD".getBytes();
//        Logger.d(TAG, "onPcmReceive head size = " + head.length + ", int = " + byteArrayToInt(head, 0));
    }

    @Override
    public boolean matchingType(int type) {
        return mDataType == type;
    }

    @Override
    public boolean onMsgReceive(DataInputStream dataReader, DataOutputStream dataWriter) throws Exception {
        boolean ret = false;

        byte[] buffer = new byte[1024*320];
        int byteCount = 0;
        //循环从输入流读取 buffer字节
        byteCount = dataReader.read(buffer);
//        Logger.i(TAG, " onMsgReceive() -> byteCount = " + byteCount);
        if (byteCount > 0) {
            byte[] data = new byte[byteCount];
            System.arraycopy(buffer, 0, data, 0, Math.min(buffer.length, byteCount));

            //将读取的数据向外传递
            controlData(byteCount, data);

            ret = true;
        }

        return ret;
    }

    private void controlData(int length, byte[] data) {
        if (length >= PickUpContent.HEAD_LENGTH) {
            byte[] header = new byte[PickUpContent.HEAD_LENGTH];
            System.arraycopy(data, 0, header, 0, PickUpContent.HEAD_LENGTH);
            // 分析协议头
            PickUpContent pickUpContent = analysisContent(header);
            if (pickUpContent != null && pickUpContent.header == PickUpContent.DEFAULT_HEADER) {

                byte[] content = new byte[length - PickUpContent.HEAD_LENGTH];
                System.arraycopy(data, PickUpContent.HEAD_LENGTH, content, 0, content.length);

                // 选择标准协议头来初始化激活词截取条件
                if (pickUpContent.pkgType == PickUpContent.PKG_TYPE_HEAD) {
                    // 标准协议头
                    mNeedActivationLength = pickUpContent.triggerLength;
                    mNeedActivationStart = pickUpContent.triggerStart;
                    mTotalActivationLength = pickUpContent.triggerLength;
                    mNeedGetActivation = true;
                }

                // 1. 截取激活词数据并发送
                byte[] pickup = cutActivationPcm(content);

                // 2. 发送拾音数据
                if (pickup != null && pickup.length > 0) {
                    sendPickupPcm(pickup.length, pickup);
                }

                // 3. 发送全部激活+拾音数据
                if (content.length > 0) {
                    sendTotalPcm(content.length, content);
                }
            } else {
                sendTotalPcm(length, data);
            }
        }
    }

    /**
     * 截取激活词数据并发送
     *
     * @param data
     * @return 返回剩余的拾音数据
     */
    private byte[] cutActivationPcm(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        byte[] pickup = null;
        byte[] activation = null;
        if (mNeedActivationStart < 0) {
            mNeedActivationStart = 0;
        }

        if (mNeedGetActivation) {
            if (mNeedActivationStart < data.length) {
                // 存在有效数据

                // 计算有效可用数据长度
                int useLength = data.length - mNeedActivationStart;

                if (mNeedActivationLength > 0) {
                    // 需要截取激活词

                    if ( mNeedActivationLength > useLength ) {
                        // 还需截取的比当前可用数据多，截取可用长度数据
                        activation = new byte[useLength];
                        System.arraycopy(data, mNeedActivationStart, activation, 0, useLength);
                        mNeedActivationLength = mNeedActivationLength - useLength;
                    } else {
                        // 还需截取的比当前可用数据少，截取还需长度数据
                        activation = new byte[mNeedActivationLength];
                        System.arraycopy(data, mNeedActivationStart, activation, 0, mNeedActivationLength);

                        // 计算剩余拾音数据长度，并截取
                        int pickLength = useLength - mNeedActivationLength;
                        pickup = new byte[pickLength];
                        System.arraycopy(data, mNeedActivationStart + mNeedActivationLength, pickup, 0, pickLength);

                        // 已经完成截取任务，需截取数据归零
                        mNeedActivationLength = 0;
                        mNeedGetActivation = false;
                    }
                } else {
                    // 无需截取激活词，已经完成截取任务，需截取数据归零
                    mNeedActivationLength = 0;
                    mNeedGetActivation = false;
                    // 截取拾音数据
                    pickup = new byte[useLength];
                    System.arraycopy(data, mNeedActivationStart, pickup, 0, useLength);
                }
            } else {
                // 全部长度都是无效数据，无需截取激活和拾音
                mNeedActivationStart = mNeedActivationStart - data.length;
            }
        } else {
            pickup = data;
        }

//        Logger.d(TAG, " cutActivationPcm activation = " + (activation == null ? -1 : activation.length));

        if (activation != null && activation.length > 0) {
            sendActivationPcm(activation.length, activation, mTotalActivationLength);
        }
        return pickup;
    }

    private PickUpContent analysisContent(byte[] data){
        PickUpContent content = null;
        if (data != null && data.length == PickUpContent.HEAD_LENGTH) {
            content = new PickUpContent();

            content.header = byteArrayToInt(data, 0);
            content.pkgTotalSize = byteArrayToInt(data, 4);
            content.pkgType = byteArrayToInt(data, 8);
            content.triggerStart = byteArrayToInt(data, 12);
            content.triggerLength = byteArrayToInt(data, 16);
            content.dataLen = byteArrayToInt(data, 20);

//            Logger.d(TAG, "analysisContent : " + content.toString());
        }

        return content;
    }

    private int byteArrayToInt(byte[] data, int start){

        byte[] b = new byte[4];
        System.arraycopy(data, start, b, 0, 4);

        int MASK = 0xFF;
        int result = 0;
        result = b[0] & MASK;
        result = result + ((b[1] & MASK) << 8);
        result = result + ((b[2] & MASK) << 16);
        result = result + ((b[3] & MASK) << 24);

//        result = b[3] & MASK;
//        result = result + ((b[2] & MASK) << 8);
//        result = result + ((b[1] & MASK) << 16);
//        result = result + ((b[0] & MASK) << 24);

        return result;
    }

    /**
     * 激活拾音pcm数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     */
    private void sendTotalPcm(int length, byte[] data){
//        Logger.d(TAG, " sendTotalPcm len = " + length);
        if (mPcmListener != null) {
            mPcmListener.onPcmReceive(length, data);
        }

    }

    /**
     * 单纯激活词语音数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     */
    private void sendActivationPcm(int length, byte[] data, int totalLength) {
//        Logger.d(TAG, "sendActivationPcm len = " + length + ", totalLength = " + totalLength);
        if (mPcmListener != null) {
            mPcmListener.onActivationReceive(length, data, totalLength);
        }
    }

    /**
     * 单纯拾音语音数据获取后处理
     *
     * @param length 数据长度
     * @param data 数据内容
     */
    private void sendPickupPcm(int length, byte[] data) {
//        Logger.d(TAG, " sendPickupPcm len = " + length);
        if (mPcmListener != null) {
            mPcmListener.onPickupReceive(length, data);
        }

    }

    @Override
    public void onDestroy() {
        mPcmListener = null;
    }
}

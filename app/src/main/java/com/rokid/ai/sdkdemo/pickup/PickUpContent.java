package com.rokid.ai.sdkdemo.pickup;

/**
 * Func: Socket 算法激活、拾音 数据实体类
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/6
 */
public class PickUpContent {

    public static final int HEAD_LENGTH = 24;

    public static final int PKG_TYPE_HEAD = 0;

    public static final int PKG_TYPE_DATA = 2;

    public static final int DEFAULT_HEADER = 1145128264;

    /**
     * 'H''E''A''D'
     */
    public int header;
    /**
     * total pkgSize
     */
    public int pkgTotalSize;
    /**
     * type: 0 for wake up content, 1 for  speech data
     */
    public int pkgType;
    /**
     * wakeup words start address
     */
    public int triggerStart;
    /**
     * wakeup words length, -1 express it is invalid
     */
    public int triggerLength;
    /**
     * dataLen + sizeof(R2Content) = pkgTotalSize
     */
    public int dataLen;

    @Override
    public String toString() {
        return "PickUpContent[ header=" + header + ", pkgTotalSize="  + pkgTotalSize + ", pkgType=" + pkgType
                + ", triggerStart=" + triggerStart + ", triggerLength=" + triggerLength + ", dataLen=" + dataLen + " ]";
    }
}

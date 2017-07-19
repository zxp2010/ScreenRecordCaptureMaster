package com.yixia.screenrecordlib.record.audio;

import java.nio.ByteBuffer;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class AudioDataBean {
    public ByteBuffer rawBuffer;
    public int length;

    public AudioDataBean() {
    }

    public AudioDataBean(ByteBuffer rawBuffer, int length) {
        this.rawBuffer = rawBuffer;
        this.length = length;
    }

    public ByteBuffer getRawBuffer() {
        return rawBuffer;
    }

    public void setRawBuffer(ByteBuffer rawBuffer) {
        this.rawBuffer = rawBuffer;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "AudioDataBean{" +
                "rawBuffer=" + rawBuffer +
                ", length=" + length +
                '}';
    }
}

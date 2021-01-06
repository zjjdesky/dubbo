/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.remoting.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * DynamicChannelBuffer 可以认为是其他 ChannelBuffer 的装饰器，
 * 它可以为其他 ChannelBuffer 添加动态扩展容量的功能
 */
public class DynamicChannelBuffer extends AbstractChannelBuffer {

    /**
     * 用于创建被修饰的 HeapChannelBuffer 对象的 ChannelBufferFactory 工厂
     * 默认为 HeapChannelBufferFactory。
     */
    private final ChannelBufferFactory factory;

    /**
     * 被修饰的ChannelBuffer，默认为HeapChannelBuffer
     */
    private ChannelBuffer buffer;

    public DynamicChannelBuffer(int estimatedLength) {
        this(estimatedLength, HeapChannelBufferFactory.getInstance());
    }

    public DynamicChannelBuffer(int estimatedLength, ChannelBufferFactory factory) {
        if (estimatedLength < 0) {
            throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
        }
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factory = factory;
        buffer = factory.getBuffer(estimatedLength);
    }

    /**
     * ensureWritableBytes() 方法如果检测到底层 ChannelBuffer 对象的空间不足，
     * 则会创建一个新的 ChannelBuffer（空间扩大为原来的两倍），
     * 然后将原来 ChannelBuffer 中的数据拷贝到新 ChannelBuffer 中，
     * 最后将 buffer 字段指向新 ChannelBuffer 对象，完成整个扩容操作。
     * @param minWritableBytes
     */
    @Override
    public void ensureWritableBytes(int minWritableBytes) {
        if (minWritableBytes <= writableBytes()) {
            return;
        }

        int newCapacity;
        if (capacity() == 0) {
            newCapacity = 1;
        } else {
            newCapacity = capacity();
        }
        int minNewCapacity = writerIndex() + minWritableBytes;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;
        }

        ChannelBuffer newBuffer = factory().getBuffer(newCapacity);
        newBuffer.writeBytes(buffer, 0, writerIndex());
        buffer = newBuffer;
    }


    @Override
    public int capacity() {
        return buffer.capacity();
    }


    @Override
    public ChannelBuffer copy(int index, int length) {
        DynamicChannelBuffer copiedBuffer = new DynamicChannelBuffer(Math.max(length, 64), factory());
        copiedBuffer.buffer = buffer.copy(index, length);
        copiedBuffer.setIndex(0, length);
        return copiedBuffer;
    }


    @Override
    public ChannelBufferFactory factory() {
        return factory;
    }


    @Override
    public byte getByte(int index) {
        return buffer.getByte(index);
    }


    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }


    @Override
    public void getBytes(int index, ByteBuffer dst) {
        buffer.getBytes(index, dst);
    }


    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }


    @Override
    public void getBytes(int index, OutputStream dst, int length) throws IOException {
        buffer.getBytes(index, dst, length);
    }


    @Override
    public boolean isDirect() {
        return buffer.isDirect();
    }


    @Override
    public void setByte(int index, int value) {
        buffer.setByte(index, value);
    }


    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
    }


    @Override
    public void setBytes(int index, ByteBuffer src) {
        buffer.setBytes(index, src);
    }


    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
    }


    @Override
    public int setBytes(int index, InputStream src, int length) throws IOException {
        return buffer.setBytes(index, src, length);
    }


    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return buffer.toByteBuffer(index, length);
    }

    @Override
    public void writeByte(int value) {
        ensureWritableBytes(1);
        super.writeByte(value);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
        ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        ensureWritableBytes(src.remaining());
        super.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        ensureWritableBytes(length);
        return super.writeBytes(in, length);
    }


    @Override
    public byte[] array() {
        return buffer.array();
    }


    @Override
    public boolean hasArray() {
        return buffer.hasArray();
    }


    @Override
    public int arrayOffset() {
        return buffer.arrayOffset();
    }
}

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
package org.apache.dubbo.remoting;

import org.apache.dubbo.common.URL;

import java.net.InetSocketAddress;

/**
 * Endpoint. (API/SPI, Prototype, ThreadSafe)
 *
 * 端点
 * 我们可以通过一个 ip 和 port 唯一确定一个端点，两个端点之间会创建 TCP 连接，可以双向传输数据。
 * Dubbo 将 Endpoint 之间的 TCP 连接抽象为通道（Channel），
 * 将发起请求的 Endpoint 抽象为客户端（Client），
 * 将接收请求的 Endpoint 抽象为服务端（Server）。
 *
 * @see org.apache.dubbo.remoting.Channel
 * @see org.apache.dubbo.remoting.Client
 * @see RemotingServer
 */
public interface Endpoint {

    /**
     * get url.
     *
     * @return url
     */
    URL getUrl();

    /**
     * get channel handler.
     *
     * @return channel handler
     */
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     *
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * send message.
     *
     * @param message
     * @throws RemotingException
     */
    void send(Object message) throws RemotingException;

    /**
     * send message.
     *
     * @param message
     * @param sent    already sent to socket?
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * close the channel.
     */
    void close();

    /**
     * Graceful close the channel.
     */
    void close(int timeout);

    void startClose();

    /**
     * is closed.
     *
     * @return closed
     */
    boolean isClosed();

}
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
package org.apache.dubbo.rpc.protocol;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.ExporterListener;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.InvokerListener;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.ProtocolServer;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.listener.ListenerExporterWrapper;
import org.apache.dubbo.rpc.listener.ListenerInvokerWrapper;

import java.util.Collections;
import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.EXPORTER_LISTENER_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.INVOKER_LISTENER_KEY;

/**
 * ListenerProtocol
 */
@Activate(order = 200)
public class ProtocolListenerWrapper implements Protocol {

    private final Protocol protocol;

    public ProtocolListenerWrapper(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    @Override
    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        // 注册中心
        if (UrlUtils.isRegistry(invoker.getUrl())) {
            return protocol.export(invoker);
        }
        /**
         * // 暴露服务，创建 Exporter 对象
         * Exporter<T> exporter = protocol.export(invoker);
         * // 获得 ExporterListener 数组
         * List<ExporterListener> listeners = Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(ExporterListener.class).getActivateExtension(invoker.getUrl(), Constants.EXPORTER_LISTENER_KEY));
         * // 创建带 ExporterListener 的 Exporter 对象
         *return new ListenerExporterWrapper<T>(exporter, listeners);
         */
        return new ListenerExporterWrapper<T>(protocol.export(invoker),
                Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(ExporterListener.class)
                        .getActivateExtension(invoker.getUrl(), EXPORTER_LISTENER_KEY)));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if (UrlUtils.isRegistry(url)) {
            return protocol.refer(type, url);
        }
        return new ListenerInvokerWrapper<T>(protocol.refer(type, url),
                Collections.unmodifiableList(
                        ExtensionLoader.getExtensionLoader(InvokerListener.class)
                                .getActivateExtension(url, INVOKER_LISTENER_KEY)));
    }

    @Override
    public void destroy() {
        protocol.destroy();
    }

    @Override
    public List<ProtocolServer> getServers() {
        return protocol.getServers();
    }
}

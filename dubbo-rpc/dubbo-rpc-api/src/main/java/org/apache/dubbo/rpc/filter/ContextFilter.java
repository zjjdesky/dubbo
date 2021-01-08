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
package org.apache.dubbo.rpc.filter;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.TimeoutCountDown;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.dubbo.common.constants.CommonConstants.DUBBO_VERSION_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.INTERFACE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;
import static org.apache.dubbo.common.constants.CommonConstants.REMOTE_APPLICATION_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.TIMEOUT_ATTACHMENT_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.TIMEOUT_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.TIME_COUNTDOWN_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.rpc.Constants.ASYNC_KEY;
import static org.apache.dubbo.rpc.Constants.FORCE_USE_TAG;
import static org.apache.dubbo.rpc.Constants.TOKEN_KEY;


/**
 * ContextFilter set the provider RpcContext with invoker, invocation, local port it is using and host for
 * current execution thread.
 * ContextFilter 是 Provider 端的一个 Filter 实现，它主要用来初始化 Provider 端的 RpcContext。
 * @see RpcContext
 */
@Activate(group = PROVIDER, order = -10000)
public class ContextFilter implements Filter, Filter.Listener {

    private static final String TAG_KEY = "dubbo.tag";

    private static final Set<String> UNLOADING_KEYS;

    static {
        UNLOADING_KEYS = new HashSet<>(128);
        UNLOADING_KEYS.add(PATH_KEY);
        UNLOADING_KEYS.add(INTERFACE_KEY);
        UNLOADING_KEYS.add(GROUP_KEY);
        UNLOADING_KEYS.add(VERSION_KEY);
        UNLOADING_KEYS.add(DUBBO_VERSION_KEY);
        UNLOADING_KEYS.add(TOKEN_KEY);
        UNLOADING_KEYS.add(TIMEOUT_KEY);
        UNLOADING_KEYS.add(TIMEOUT_ATTACHMENT_KEY);

        // Remove async property to avoid being passed to the following invoke chain.
        UNLOADING_KEYS.add(ASYNC_KEY);
        UNLOADING_KEYS.add(TAG_KEY);
        UNLOADING_KEYS.add(FORCE_USE_TAG);
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取attachments
        Map<String, Object> attachments = invocation.getObjectAttachments();
        if (attachments != null) {
            // 过滤掉UNLOADING_KEYS集合
            Map<String, Object> newAttach = new HashMap<>(attachments.size());
            for (Map.Entry<String, Object> entry : attachments.entrySet()) {
                String key = entry.getKey();
                if (!UNLOADING_KEYS.contains(key)) {
                    newAttach.put(key, entry.getValue());
                }
            }
            attachments = newAttach;
        }

        RpcContext context = RpcContext.getContext();
        context.setInvoker(invoker)
                .setInvocation(invocation)
//                .setAttachments(attachments)  // merged from dubbox
                .setLocalAddress(invoker.getUrl().getHost(), invoker.getUrl().getPort());
        String remoteApplication = (String) invocation.getAttachment(REMOTE_APPLICATION_KEY);
        if (StringUtils.isNotEmpty(remoteApplication)) {
            context.setRemoteApplicationName(remoteApplication);
        } else {
            context.setRemoteApplicationName((String) context.getAttachment(REMOTE_APPLICATION_KEY));
        }

        long timeout = RpcUtils.getTimeout(invocation, -1);
        if (timeout != -1) { // 设置超时时间
            context.set(TIME_COUNTDOWN_KEY, TimeoutCountDown.newCountDown(timeout, TimeUnit.MILLISECONDS));
        }

        // merged from dubbox
        // we may already added some attachments into RpcContext before this filter (e.g. in rest protocol)
        if (attachments != null) { // 想RpcContext中设置attachments
            if (context.getObjectAttachments() != null) {
                context.getObjectAttachments().putAll(attachments);
            } else {
                context.setObjectAttachments(attachments);
            }
        }

        if (invocation instanceof RpcInvocation) { // 向Invocation设置Invoker
            ((RpcInvocation) invocation).setInvoker(invoker);
        }

        try {
            // 在整个调用过程中，需要保持当前RpcContext不被删除，这里会将remove开关关掉，
            // 这样，removeContext()方法不会删除LOCAL RpcContext了
            context.clearAfterEachInvoke(false);
            return invoker.invoke(invocation);
        } finally {
            // 重置remove开关
            context.clearAfterEachInvoke(true);
            // IMPORTANT! For async scenario, we must remove context from current thread, so we always create a new RpcContext for the next invoke for the same thread.
            // 清理RpcContext，当前线程处理下一个调用的时候，会创建新的RpcContext
            RpcContext.removeContext(true);
            RpcContext.removeServerContext();
        }
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // pass attachments to result
        appResponse.addObjectAttachments(RpcContext.getServerContext().getObjectAttachments());
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {

    }
}

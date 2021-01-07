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
package org.apache.dubbo.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * It's recommended to implement Filter.Listener directly for callback registration, check the default implementation,
 * see {@link org.apache.dubbo.rpc.filter.ExceptionFilter}, for example.
 * <p>
 * If you do not want to share Listener instance between RPC calls. You can use ListenableFilter
 * to keep a 'one Listener each RPC call' model.
 *
 * 在执行 invoke() 调用之前，我们可以调用 addListener() 方法添加 Filter.Listener 实例进行监听，
 * 完成一次 invoke() 方法之后，这些添加的 Filter.Listener 实例就会立即从 listeners 集合中删除，
 * 也就是说，这些 Filter.Listener 实例不会在调用之间共享。
 */
public abstract class ListenableFilter implements Filter {

    protected Listener listener = null;
    /**
     * listeners集合
     * 用来记录一次请求需要触发的监听器
     */
    protected final ConcurrentMap<Invocation, Listener> listeners = new ConcurrentHashMap<>();

    public Listener listener() {
        return listener;
    }

    public Listener listener(Invocation invocation) {
        Listener invListener = listeners.get(invocation);
        if (invListener == null) {
            invListener = listener;
        }
        return invListener;
    }

    public void addListener(Invocation invocation, Listener listener) {
        listeners.putIfAbsent(invocation, listener);
    }

    public void removeListener(Invocation invocation) {
        listeners.remove(invocation);
    }
}

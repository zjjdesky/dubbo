package org.apache.dubbo.demo.jjzhou.dubbo.spi;

import org.apache.dubbo.common.extension.ExtensionLoader;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 2:15 下午
 */
public class Main {

    public static void main(String[] args) {
        ExtensionLoader<MyLog> extensionLoader = ExtensionLoader.getExtensionLoader(MyLog.class);
        MyLog myLogback = extensionLoader.getExtension("myLogback");
        myLogback.log("hello world!");
    }
}

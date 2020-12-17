package org.apache.dubbo.demo.jjzhou.jdk.spi;

import java.util.ServiceLoader;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 1:58 下午
 */
public class Main {

    public static void main(String[] args) {
        ServiceLoader<MyLog> myLogs = ServiceLoader.load(MyLog.class);
        myLogs.forEach(log -> log.log("hello world!"));
    }
}

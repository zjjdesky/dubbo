package org.apache.dubbo.demo.jjzhou.jdk.spi.impl;

import org.apache.dubbo.demo.jjzhou.jdk.spi.MyLog;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 1:57 下午
 */
public class MyLog4j implements MyLog {

    @Override
    public void log(String info) {
        System.out.println("MyLog4j: " + info);
    }
}

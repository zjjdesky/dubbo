package org.apache.dubbo.demo.jjzhou.jdk.spi.impl;

import org.apache.dubbo.demo.jjzhou.jdk.spi.MyLog;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 1:56 下午
 */
public class MyLogback implements MyLog {

    @Override
    public void log(String info) {
        System.out.println("MyLogback: " + info);
    }
}

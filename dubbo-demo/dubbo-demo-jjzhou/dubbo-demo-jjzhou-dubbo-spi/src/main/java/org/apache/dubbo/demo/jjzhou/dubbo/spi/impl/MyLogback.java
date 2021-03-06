package org.apache.dubbo.demo.jjzhou.dubbo.spi.impl;

import org.apache.dubbo.demo.jjzhou.dubbo.spi.MyLog;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 2:16 下午
 */
public class MyLogback implements MyLog {

    @Override
    public void log(String info) {
        System.out.println("MyLogback: " + info);
    }
}

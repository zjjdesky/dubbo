package org.apache.dubbo.demo.jjzhou.dubbo.spi;

import org.apache.dubbo.common.extension.SPI;

/**
 * @author zhoujianjun
 * @description
 * @date 2020/12/17 2:15 下午
 */
@SPI
public interface MyLog {

    void log(String info);
}

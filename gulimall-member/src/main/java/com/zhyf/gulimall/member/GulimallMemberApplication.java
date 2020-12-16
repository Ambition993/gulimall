package com.zhyf.gulimall.member;

import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.sun.corba.se.impl.activation.ServerMain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.zhyf.gulimall.member.openfeign")
public class GulimallMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }
}
//远程调用其他的服务
// 先引入 openfeign
// 写一个接口
//使用注解 开启功能@EnableFeignClients
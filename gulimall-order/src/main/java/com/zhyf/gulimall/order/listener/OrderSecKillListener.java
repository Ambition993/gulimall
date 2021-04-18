package com.zhyf.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zhyf.common.to.mq.SeckillOrderTo;
import com.zhyf.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSecKillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        log.info("ready to create seckillOrder");
        orderService.createSeckillOrder(seckillOrderTo);
    }
}

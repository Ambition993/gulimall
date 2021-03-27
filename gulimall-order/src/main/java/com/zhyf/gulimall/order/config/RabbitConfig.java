package com.zhyf.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitmq
     */
    @Autowired
    RabbitTemplate rabbitTemplate;


    @PostConstruct  // 对象创建完成后执行这个方法
    public void initRabbitTemplate() {
        // 服务器收到消息的回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData  当前消息的唯一关联数据 唯一Id
             * @param ack   是否失败
             * @param cause 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm" + "==>>>" + ack + "====>>" + cause);
            }
        });
        // 消息抵达队列的确认回调 只要消息没有投递给指定的队列 就触发这个 类似于失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *
             * @param message 投递失败的消息的详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本类容
             * @param exchange  当前消息发的的交换机
             * @param routingKey 当前消息使用的路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println(message + "__" + replyCode + "__" + replyText + "__" + exchange + "__" + routingKey);
            }
        });
    }
}

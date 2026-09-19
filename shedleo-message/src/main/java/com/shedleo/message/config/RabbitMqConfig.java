package com.shedleo.message.config;

import com.shedleo.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（壳）
 *
 * 消息服务需要监听：
 *  - message.warn.queue  → 库存预警消息
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(MqConstant.MESSAGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue messageWarnQueue() {
        return QueueBuilder.durable(MqConstant.MESSAGE_WARN_QUEUE).build();
    }

    @Bean
    public Binding messageWarnBinding() {
        return BindingBuilder.bind(messageWarnQueue())
                .to(messageExchange())
                .with(MqConstant.MESSAGE_WARN_ROUTING_KEY);
    }
}

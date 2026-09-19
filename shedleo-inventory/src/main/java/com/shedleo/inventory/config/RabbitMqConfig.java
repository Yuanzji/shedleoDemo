package com.shedleo.inventory.config;

import com.shedleo.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("RabbitMQ 发送确认失败: " + cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("RabbitMQ 消息路由失败: " + returned.getMessage());
        });
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange(MqConstant.INVENTORY_EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryRollbackQueue() {
        return QueueBuilder.durable(MqConstant.INVENTORY_ROLLBACK_QUEUE).build();
    }

    @Bean
    public Binding inventoryRollbackBinding() {
        return BindingBuilder.bind(inventoryRollbackQueue())
                .to(inventoryExchange())
                .with(MqConstant.INVENTORY_ROLLBACK_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(MqConstant.PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(MqConstant.MESSAGE_EXCHANGE, true, false);
    }
}

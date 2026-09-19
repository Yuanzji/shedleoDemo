package com.shedleo.payment.config;

import com.shedleo.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（壳）
 *
 * 支付服务需要监听：
 *  - payment.refund.queue  → 订单退款消息
 *  - payment.settle.queue  → 订单结算消息
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(MqConstant.PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentRefundQueue() {
        return QueueBuilder.durable(MqConstant.PAYMENT_REFUND_QUEUE).build();
    }

    @Bean
    public Binding paymentRefundBinding() {
        return BindingBuilder.bind(paymentRefundQueue())
                .to(paymentExchange())
                .with(MqConstant.PAYMENT_REFUND_ROUTING_KEY);
    }

    @Bean
    public Queue paymentSettleQueue() {
        return QueueBuilder.durable(MqConstant.PAYMENT_SETTLE_QUEUE).build();
    }

    @Bean
    public Binding paymentSettleBinding() {
        return BindingBuilder.bind(paymentSettleQueue())
                .to(paymentExchange())
                .with(MqConstant.PAYMENT_SETTLE_ROUTING_KEY);
    }
}

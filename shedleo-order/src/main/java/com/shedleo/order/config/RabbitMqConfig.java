package com.shedleo.order.config;

import com.shedleo.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置 —— TTL + DLX 实现订单超时自动关闭
 *
 * 【核心原理】：
 *  1. 下单成功后，发送一条消息到「延迟队列」order.delay.queue
 *  2. 延迟队列绑定到死信交换机 order.dlx.exchange（设置 x-dead-letter-exchange）
 *  3. 延迟队列的每条消息设置 TTL（例如 24h）
 *  4. 消息 TTL 到期后，变成死信，被路由到死信队列 order.dlx.queue
 *  5. 订单服务监听死信队列，消费后关闭超时未支付订单
 *
 * 【为什么用死信？】
 *  - 延迟队列本身不消费，仅作为"暂存区"
 *  - 避免客户端长时间持有连接
 *  - 死信队列按需消费，灵活性高
 */
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
                System.err.println("[RabbitMQ] 发送确认失败: " + cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("[RabbitMQ] 消息路由失败: " + returned.getMessage());
        });
        return rabbitTemplate;
    }

    /**
     * 死信交换机（Direct 类型）
     * 接收来自延迟队列的过期消息
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(MqConstant.ORDER_DLX_EXCHANGE, true, false);
    }

    /**
     * 延迟队列
     * —— 设置 x-dead-letter-exchange + x-dead-letter-routing-key
     * —— 消息过期后会被转发到死信交换机
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MqConstant.ORDER_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", MqConstant.ORDER_DLX_ROUTING_KEY);
        return QueueBuilder.durable(MqConstant.ORDER_DELAY_QUEUE)
                .withArguments(args)
                .build();
    }

    /**
     * 绑定：延迟队列 —— 没有实际消费，仅用于暂存
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDlxExchange())
                .with(MqConstant.ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 死信队列
     * —— 真正被监听消费的队列
     * —— 到期的延迟消息会被路由到这里
     */
    @Bean
    public Queue orderDlxQueue() {
        return QueueBuilder.durable(MqConstant.ORDER_DLX_QUEUE).build();
    }

    /**
     * 绑定：死信交换机 → 死信队列
     */
    @Bean
    public Binding orderDlxBinding() {
        return BindingBuilder.bind(orderDlxQueue())
                .to(orderDlxExchange())
                .with(MqConstant.ORDER_DLX_ROUTING_KEY);
    }

    // ===== 其他业务队列 =====

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

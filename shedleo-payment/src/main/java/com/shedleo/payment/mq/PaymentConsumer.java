package com.shedleo.payment.mq;

import com.rabbitmq.client.Channel;
import com.shedleo.common.constant.MqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 支付服务 MQ 消费者
 *
 * 【壳子说明】：此处只打印日志。
 *  实际应调用微信/支付宝退款和分账 API。
 */
@Slf4j
@Component
public class PaymentConsumer {

    @RabbitListener(queues = MqConstant.PAYMENT_REFUND_QUEUE)
    public void handleRefund(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("[PAYMENT-SHELL] 收到退款消息: {}", msg);
            log.info("[PAYMENT-SHELL] ★ 此处应调用微信/支付宝退款 API ★");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("处理退款消息失败", e);
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = MqConstant.PAYMENT_SETTLE_QUEUE)
    public void handleSettle(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("[PAYMENT-SHELL] 收到结算消息: {}", msg);
            log.info("[PAYMENT-SHELL] ★ 此处应调用微信/支付宝分账 API ★");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("处理结算消息失败", e);
            channel.basicNack(tag, false, false);
        }
    }
}

package com.shedleo.message.mq;

import com.rabbitmq.client.Channel;
import com.shedleo.common.constant.MqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 消息服务 MQ 消费者 —— 监听库存预警
 *
 * 【壳子说明】：此处只打印日志。
 *  实际应：
 *  1. 发送短信通知商家
 *  2. 发送站内信到商家后台
 *  3. 记录消息发送日志
 */
@Slf4j
@Component
public class MessageConsumer {

    @RabbitListener(queues = MqConstant.MESSAGE_WARN_QUEUE)
    public void handleWarn(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("[MESSAGE-SHELL] 收到库存预警消息: {}", msg);
            log.info("[MESSAGE-SHELL] ★ 此处应发送短信/站内信通知商家 ★");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("处理预警消息失败", e);
            channel.basicNack(tag, false, false);
        }
    }
}

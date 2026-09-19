package com.shedleo.order.mq;

import com.rabbitmq.client.Channel;
import com.shedleo.common.constant.MqConstant;
import com.shedleo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 订单超时关闭消费者
 *
 * 【RabbitMQ TTL + DLX 流程回顾】：
 *  1. 下单成功 → 发送消息到 order.delay.queue（设置 TTL=24h）
 *  2. 延迟队列绑定了 x-dead-letter-exchange = order.dlx.exchange
 *  3. 24h 后消息过期 → 自动投递到 order.dlx.exchange
 *  4. 交换机路由到 order.dlx.queue（死信队列）
 *  5. 本类监听死信队列，消费后执行关闭订单逻辑
 *
 *  使用场景：
 *  - 订单创建 24h 未支付 → 自动关闭
 *  - Redis Key 过期通知（需额外配置）
 *  - 任何需要"延迟执行"的业务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConstant.ORDER_DLX_QUEUE)
    public void handleOrderTimeout(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String orderNo = (String) msg.get("orderNo");
            String type = (String) msg.get("type");
            log.info("收到死信消息: type={}, orderNo={}", type, orderNo);

            if ("order-timeout".equals(type) && orderNo != null) {
                orderService.cancelOrder(orderNo, "订单超时未支付自动关闭");
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理死信消息失败: {}", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}

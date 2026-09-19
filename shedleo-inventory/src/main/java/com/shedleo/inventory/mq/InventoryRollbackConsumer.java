package com.shedleo.inventory.mq;

import com.rabbitmq.client.Channel;
import com.shedleo.common.constant.MqConstant;
import com.shedleo.inventory.dto.RollbackRequest;
import com.shedleo.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRollbackConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = MqConstant.INVENTORY_ROLLBACK_QUEUE)
    public void handleRollback(RollbackRequest request, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到库存回滚消息: skuId={}, orderId={}, qty={}",
                    request.getSkuId(), request.getOrderId(), request.getQuantity());
            inventoryService.rollback(request);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("库存回滚失败", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}

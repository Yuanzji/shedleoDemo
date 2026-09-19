package com.shedleo.inventory.job;

import com.shedleo.common.constant.MqConstant;
import com.shedleo.inventory.entity.Inventory;
import com.shedleo.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockWarnJob {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 60000)
    public void checkLowStock() {
        List<Inventory> lowList = inventoryService.listLowStock();
        if (lowList.isEmpty()) {
            return;
        }
        log.warn("发现 {} 个 SKU 库存低于安全阈值", lowList.size());
        for (Inventory inv : lowList) {
            Map<String, Object> msg = Map.of(
                    "skuId", inv.getSkuId(),
                    "stock", inv.getStock(),
                    "safeStock", inv.getSafeStock(),
                    "merchantId", inv.getMerchantId(),
                    "message", "SKU " + inv.getSkuId() + " 库存低于安全阈值"
            );
            rabbitTemplate.convertAndSend(
                    MqConstant.MESSAGE_EXCHANGE,
                    MqConstant.MESSAGE_WARN_ROUTING_KEY,
                    msg);
        }
    }
}

package com.shedleo.order.client;

import com.shedleo.common.result.Result;
import com.shedleo.order.client.fallback.InventoryClientFallback;
import com.shedleo.order.dto.InventoryRollbackDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "shedleo-inventory", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @PostMapping("/inventory/deduct")
    Result<Boolean> deductBatch(@RequestBody List<DeductItem> items);

    @PostMapping("/inventory/rollback")
    Result<Boolean> rollback(@RequestBody InventoryRollbackDTO dto);

    @lombok.Data
    class DeductItem {
        private Long skuId;
        private Integer quantity;
        private Long orderId;
        private Boolean presale;
    }
}

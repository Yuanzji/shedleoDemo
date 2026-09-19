package com.shedleo.order.client.fallback;

import com.shedleo.common.result.Result;
import com.shedleo.order.client.InventoryClient;
import com.shedleo.order.dto.InventoryRollbackDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public Result<Boolean> deductBatch(List<DeductItem> items) {
        log.error("Feign 降级: 库存扣减失败");
        return Result.fail("库存服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> rollback(InventoryRollbackDTO dto) {
        log.error("Feign 降级: 库存回滚失败");
        return Result.fail("库存服务暂时不可用，请稍后重试");
    }
}

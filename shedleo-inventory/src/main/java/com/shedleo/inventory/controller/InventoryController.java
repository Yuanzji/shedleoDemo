package com.shedleo.inventory.controller;

import com.shedleo.common.result.Result;
import com.shedleo.inventory.dto.DeductRequest;
import com.shedleo.inventory.dto.RollbackRequest;
import com.shedleo.inventory.entity.Inventory;
import com.shedleo.inventory.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Api(tags = "库存管理")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @ApiOperation("扣减库存（供订单服务 Feign 调用）")
    @PostMapping("/deduct")
    public Result<Boolean> deduct(@Valid @RequestBody DeductRequest request) {
        log.info("扣减库存请求: {}", request);
        boolean result = inventoryService.deduct(request);
        return Result.success(result);
    }

    @ApiOperation("回滚库存")
    @PostMapping("/rollback")
    public Result<Boolean> rollback(@Valid @RequestBody RollbackRequest request) {
        log.info("库存回滚请求: {}", request);
        boolean result = inventoryService.rollback(request);
        return Result.success(result);
    }

    @ApiOperation("查询单个 SKU 库存")
    @GetMapping("/{skuId}")
    public Result<Integer> getStock(@PathVariable Long skuId) {
        Integer stock = inventoryService.getStock(skuId);
        return Result.success(stock);
    }

    @ApiOperation("商家库存列表")
    @GetMapping("/list")
    public Result<List<Inventory>> list(@RequestParam(defaultValue = "1") Long merchantId) {
        return Result.success(inventoryService.listByMerchant(merchantId));
    }

    @ApiOperation("创建预售排期")
    @PostMapping("/presale")
    public Result<Boolean> presale(@Valid @RequestBody DeductRequest request) {
        request.setPresale(true);
        boolean result = inventoryService.deduct(request);
        return Result.success(result);
    }
}

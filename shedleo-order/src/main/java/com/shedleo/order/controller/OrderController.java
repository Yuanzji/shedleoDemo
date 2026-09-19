package com.shedleo.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shedleo.common.result.Result;
import com.shedleo.order.dto.CreateOrderDTO;
import com.shedleo.order.dto.RefundDTO;
import com.shedleo.order.entity.Order;
import com.shedleo.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<Order> create(@Valid @RequestBody CreateOrderDTO dto) {
        log.info("创建订单请求: userId={}, skuCount={}", dto.getUserId(), dto.getItems().size());
        Order order = orderService.createOrder(dto);
        return Result.success(order);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/{orderNo}")
    public Result<Order> detail(@PathVariable String orderNo) {
        return Result.success(orderService.getByOrderNo(orderNo));
    }

    @ApiOperation("分页查询订单")
    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.page(current, size, userId, status));
    }

    @ApiOperation("申请退款")
    @PostMapping("/refund/apply")
    public Result<Boolean> refund(@Valid @RequestBody RefundDTO dto) {
        log.info("退款申请: orderNo={}, amount={}", dto.getOrderNo(), dto.getAmount());
        return Result.success(orderService.applyRefund(dto));
    }

    @ApiOperation("确认收货")
    @PostMapping("/confirm")
    public Result<Boolean> confirm(@RequestParam String orderNo) {
        log.info("确认收货: orderNo={}", orderNo);
        return Result.success(orderService.confirmOrder(orderNo));
    }

    @ApiOperation("取消订单（内部接口）")
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@RequestParam String orderNo,
                                  @RequestParam(defaultValue = "手动取消") String reason) {
        log.info("取消订单: orderNo={}, reason={}", orderNo, reason);
        return Result.success(orderService.cancelOrder(orderNo, reason));
    }
}

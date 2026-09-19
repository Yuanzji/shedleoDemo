package com.shedleo.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shedleo.common.constant.MqConstant;
import com.shedleo.common.constant.OrderConstant;
import com.shedleo.common.exception.BusinessException;
import com.shedleo.common.result.Result;
import com.shedleo.common.result.ResultCode;
import com.shedleo.common.util.SnowflakeIdGenerator;
import com.shedleo.order.client.InventoryClient;
import com.shedleo.order.dto.CreateOrderDTO;
import com.shedleo.order.dto.InventoryRollbackDTO;
import com.shedleo.order.dto.RefundDTO;
import com.shedleo.order.entity.Order;
import com.shedleo.order.entity.OrderItem;
import com.shedleo.order.entity.Refund;
import com.shedleo.order.mapper.OrderItemMapper;
import com.shedleo.order.mapper.OrderMapper;
import com.shedleo.order.mapper.RefundMapper;
import com.shedleo.order.state.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单核心服务
 *
 * 核心技术点：
 *  1. 创建订单 —— 先 DB 入库，再 Feign 调用库存服务扣减库存，最后发送延迟消息
 *  2. 订单超时 —— 发送消息到延迟队列（设置 TTL），过期后自动关闭（见 DeadLetterConsumer）
 *  3. 状态流转 —— 使用 OrderStateMachine 校验
 *  4. 退款 —— 更新订单状态 + 发送 MQ 通知支付服务 + 发送 MQ 通知库存服务回滚
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RefundMapper refundMapper;
    private final InventoryClient inventoryClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderDTO dto) {
        String orderNo = generateOrderNo();

        BigDecimal totalAmount = dto.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setId(SnowflakeIdGenerator.generate());
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setMerchantId(dto.getMerchantId());
        order.setStatus(OrderConstant.STATUS_PENDING_PAYMENT);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        orderMapper.insert(order);

        List<OrderItem> itemList = new ArrayList<>();
        List<InventoryClient.DeductItem> deductItems = new ArrayList<>();

        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            OrderItem item = new OrderItem();
            item.setId(SnowflakeIdGenerator.generate());
            item.setOrderId(order.getId());
            item.setSkuId(itemDTO.getSkuId());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            item.setProductName(itemDTO.getProductName() != null
                    ? itemDTO.getProductName() : "SKU-" + itemDTO.getSkuId());
            orderItemMapper.insert(item);
            itemList.add(item);

            InventoryClient.DeductItem di = new InventoryClient.DeductItem();
            di.setSkuId(itemDTO.getSkuId());
            di.setQuantity(itemDTO.getQuantity());
            di.setOrderId(order.getId());
            di.setPresale(Boolean.TRUE.equals(itemDTO.getPresale()));
            deductItems.add(di);
        }

        Result<Boolean> deductResult = inventoryClient.deductBatch(deductItems);
        if (deductResult == null || !Boolean.TRUE.equals(deductResult.getData())) {
            throw new BusinessException(deductResult != null ? deductResult.getMessage() : "库存扣减失败");
        }

        sendDelayCloseMessage(orderNo);

        log.info("订单创建成功: orderNo={}, userId={}, totalAmount={}", orderNo, dto.getUserId(), totalAmount);
        return order;
    }

    /**
     * 发送订单超时延迟消息
     *
     * 【RabbitMQ TTL + DLX 实现延迟消息】：
     *  1. 消息发送到延迟队列 order.delay.queue
     *  2. 消息设置 TTL（24h = 24*60*60*1000 ms）
     *  3. 延迟队列配置了 x-dead-letter-exchange → order.dlx.exchange
     *  4. TTL 到期后消息自动转发到死信交换机，再路由到死信队列 order.dlx.queue
     *  5. DeadLetterConsumer 监听死信队列，消费后关闭超时订单
     */
    private void sendDelayCloseMessage(String orderNo) {
        Map<String, Object> msg = Map.of("orderNo", orderNo, "type", "order-timeout");

        MessagePostProcessor ttlProcessor = message -> {
            message.getMessageProperties()
                    .setExpiration(String.valueOf(MqConstant.ORDER_TIMEOUT_SECONDS * 1000));
            return message;
        };

        rabbitTemplate.convertAndSend(
                MqConstant.ORDER_DLX_EXCHANGE,
                MqConstant.ORDER_DELAY_ROUTING_KEY,
                msg,
                ttlProcessor);

        log.info("订单超时延迟消息已发送: orderNo={}, TTL={}s", orderNo, MqConstant.ORDER_TIMEOUT_SECONDS);
    }

    public Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    public Page<Order> page(long current, long size, Long userId, Integer status) {
        Page<Order> page = new Page<>(current, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return orderMapper.selectPage(page, wrapper);
    }

    /**
     * 取消订单（内部调用，如超时关闭）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String reason) {
        Order order = getByOrderNo(orderNo);

        if (order.getStatus() == OrderConstant.STATUS_CANCELED) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_CANCELED);
        }
        if (order.getStatus() == OrderConstant.STATUS_COMPLETED
                || order.getStatus() == OrderConstant.STATUS_REFUNDED) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_FINISHED);
        }

        int oldStatus = order.getStatus();
        OrderStateMachine.checkTransition(oldStatus, OrderConstant.STATUS_CANCELED);

        int rows = orderMapper.cancelOrder(orderNo, oldStatus, OrderConstant.STATUS_CANCELED, reason);
        if (rows == 0) {
            log.warn("订单取消并发冲突，orderNo={}", orderNo);
            return false;
        }

        rollbackInventory(order.getId());

        log.info("订单已取消: orderNo={}, reason={}", orderNo, reason);
        return true;
    }

    /**
     * 确认收货 → 通知支付服务结算
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);
        OrderStateMachine.checkTransition(order.getStatus(), OrderConstant.STATUS_CONFIRMED);

        int rows = orderMapper.updateStatusByOrderNo(
                orderNo, order.getStatus(), OrderConstant.STATUS_CONFIRMED);
        if (rows == 0) {
            log.warn("确认收货并发冲突: orderNo={}", orderNo);
            return false;
        }

        Order after = getByOrderNo(orderNo);
        after.setConfirmTime(LocalDateTime.now());
        orderMapper.updateById(after);

        sendSettleMessage(order);

        log.info("确认收货成功, 发送结算消息: orderNo={}", orderNo);
        return true;
    }

    /**
     * 退款申请
     *  —— 更新订单状态
     *  —— 发送 MQ 通知支付服务壳处理
     *  —— 发送 MQ 通知库存服务回滚库存
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(RefundDTO dto) {
        Order order = getByOrderNo(dto.getOrderNo());

        OrderStateMachine.checkTransition(order.getStatus(), OrderConstant.STATUS_REFUNDING);

        Refund refund = new Refund();
        refund.setId(SnowflakeIdGenerator.generate());
        refund.setOrderId(order.getId());
        refund.setRefundNo("RF" + SnowflakeIdGenerator.generateStr());
        refund.setAmount(dto.getAmount());
        refund.setStatus(0);
        refund.setReason(dto.getReason());
        refundMapper.insert(refund);

        int rows = orderMapper.updateStatusByOrderNo(
                dto.getOrderNo(), order.getStatus(), OrderConstant.STATUS_REFUNDING);
        if (rows == 0) {
            log.warn("退款状态更新并发冲突: orderNo={}", dto.getOrderNo());
            return false;
        }

        Map<String, Object> refundMsg = Map.of(
                "orderNo", dto.getOrderNo(),
                "refundNo", refund.getRefundNo(),
                "amount", dto.getAmount(),
                "type", "payment-refund"
        );
        rabbitTemplate.convertAndSend(
                MqConstant.PAYMENT_EXCHANGE,
                MqConstant.PAYMENT_REFUND_ROUTING_KEY,
                refundMsg);
        log.info("退款消息已发送（通知支付服务壳）: orderNo={}", dto.getOrderNo());

        rollbackInventory(order.getId());

        return true;
    }

    private void sendSettleMessage(Order order) {
        Map<String, Object> settleMsg = Map.of(
                "orderNo", order.getOrderNo(),
                "merchantId", order.getMerchantId(),
                "amount", order.getPayAmount(),
                "type", "payment-settle"
        );
        rabbitTemplate.convertAndSend(
                MqConstant.PAYMENT_EXCHANGE,
                MqConstant.PAYMENT_SETTLE_ROUTING_KEY,
                settleMsg);
    }

    private void rollbackInventory(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));

        for (OrderItem item : items) {
            InventoryRollbackDTO rb = new InventoryRollbackDTO();
            rb.setSkuId(item.getSkuId());
            rb.setQuantity(item.getQuantity());
            rb.setOrderId(orderId);

            rabbitTemplate.convertAndSend(
                    MqConstant.INVENTORY_EXCHANGE,
                    MqConstant.INVENTORY_ROLLBACK_ROUTING_KEY,
                    rb);
        }
        log.info("库存回滚消息已发送: orderId={}, 共 {} 项", orderId, items.size());
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + SnowflakeIdGenerator.nextIdStr();
    }
}

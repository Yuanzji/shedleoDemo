package com.shedleo.order.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shedleo.common.constant.OrderConstant;
import com.shedleo.order.entity.Order;
import com.shedleo.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单合并发货定时任务
 *
 * 规则：
 *  - 扫描同商家、同用户、状态为"已支付/待发货"的订单
 *  - 按最晚发货时间计算，统一设置 merge_group_id
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MergeShipJob {

    private final OrderMapper orderMapper;

    @Scheduled(fixedRate = 120000)
    public void mergePendingOrders() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .in("status", OrderConstant.STATUS_PAID, OrderConstant.STATUS_PENDING_SHIPMENT)
                        .isNull("merge_group_id"));

        if (orders.isEmpty()) {
            return;
        }

        Map<String, List<Order>> groups = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getMerchantId() + "_" + o.getUserId()));

        for (Map.Entry<String, List<Order>> entry : groups.entrySet()) {
            List<Order> groupOrders = entry.getValue();
            if (groupOrders.size() < 2) {
                continue;
            }

            String groupId = "MG" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            LocalDateTime maxShipTime = groupOrders.stream()
                    .map(Order::getExpectedDeliveryTime)
                    .filter(t -> t != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now().plusDays(3));

            for (Order order : groupOrders) {
                order.setMergeGroupId(groupId);
                order.setExpectedDeliveryTime(maxShipTime);
                orderMapper.updateById(order);
            }
            log.info("合并发货组创建: groupId={}, 订单数={}, 最晚发货={}", groupId, groupOrders.size(), maxShipTime);
        }
    }
}

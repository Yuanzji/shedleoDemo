package com.shedleo.order.state;

import com.shedleo.common.constant.OrderConstant;
import com.shedleo.common.exception.BusinessException;
import com.shedleo.common.result.ResultCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 订单状态机
 *
 * 合法流转路径：
 *  待支付(0) → 已支付(1) → 待发货(2) → 已发货(3) → 确认收货(4) → 已完成(5)
 *    ↓
 *  已取消(6)
 *
 *  任意支付后状态 → 退款中(7) → 已退款(8)
 */
public final class OrderStateMachine {

    private OrderStateMachine() {
    }

    private static final java.util.Map<Integer, Set<Integer>> TRANSITIONS = new java.util.HashMap<>();

    static {
        TRANSITIONS.put(OrderConstant.STATUS_PENDING_PAYMENT,
                new HashSet<>(Arrays.asList(
                        OrderConstant.STATUS_PAID,
                        OrderConstant.STATUS_CANCELED
                )));
        TRANSITIONS.put(OrderConstant.STATUS_PAID,
                new HashSet<>(Arrays.asList(
                        OrderConstant.STATUS_PENDING_SHIPMENT,
                        OrderConstant.STATUS_CANCELED,
                        OrderConstant.STATUS_REFUNDING
                )));
        TRANSITIONS.put(OrderConstant.STATUS_PENDING_SHIPMENT,
                new HashSet<>(Arrays.asList(
                        OrderConstant.STATUS_SHIPPED,
                        OrderConstant.STATUS_REFUNDING
                )));
        TRANSITIONS.put(OrderConstant.STATUS_SHIPPED,
                new HashSet<>(Arrays.asList(
                        OrderConstant.STATUS_CONFIRMED,
                        OrderConstant.STATUS_REFUNDING
                )));
        TRANSITIONS.put(OrderConstant.STATUS_CONFIRMED,
                new HashSet<>(Arrays.asList(
                        OrderConstant.STATUS_COMPLETED,
                        OrderConstant.STATUS_REFUNDING
                )));
        TRANSITIONS.put(OrderConstant.STATUS_REFUNDING,
                new HashSet<>(Collections.singletonList(OrderConstant.STATUS_REFUNDED)));
    }

    public static boolean canTransit(int from, int to) {
        Set<Integer> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static void checkTransition(int from, int to) {
        if (!canTransit(from, to)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR,
                    "非法状态流转: " + from + " → " + to);
        }
    }
}

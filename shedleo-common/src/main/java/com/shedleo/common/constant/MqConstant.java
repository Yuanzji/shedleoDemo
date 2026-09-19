package com.shedleo.common.constant;

public final class MqConstant {

    private MqConstant() {
    }

    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    public static final String ORDER_DLX_QUEUE = "order.dlx.queue";
    public static final String ORDER_DLX_ROUTING_KEY = "order.dlx.routing.key";

    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay.routing.key";

    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String INVENTORY_ROLLBACK_QUEUE = "inventory.rollback.queue";
    public static final String INVENTORY_ROLLBACK_ROUTING_KEY = "inventory.rollback.routing.key";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";
    public static final String PAYMENT_REFUND_ROUTING_KEY = "payment.refund.routing.key";
    public static final String PAYMENT_SETTLE_QUEUE = "payment.settle.queue";
    public static final String PAYMENT_SETTLE_ROUTING_KEY = "payment.settle.routing.key";

    public static final String MESSAGE_EXCHANGE = "message.exchange";
    public static final String MESSAGE_WARN_QUEUE = "message.warn.queue";
    public static final String MESSAGE_WARN_ROUTING_KEY = "message.warn.routing.key";

    public static final int ORDER_TIMEOUT_SECONDS = 24 * 60 * 60;
}

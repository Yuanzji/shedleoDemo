package com.shedleo.common.constant;

public final class OrderConstant {

    private OrderConstant() {
    }

    public static final int STATUS_PENDING_PAYMENT = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_PENDING_SHIPMENT = 2;
    public static final int STATUS_SHIPPED = 3;
    public static final int STATUS_CONFIRMED = 4;
    public static final int STATUS_COMPLETED = 5;
    public static final int STATUS_CANCELED = 6;
    public static final int STATUS_REFUNDING = 7;
    public static final int STATUS_REFUNDED = 8;
}

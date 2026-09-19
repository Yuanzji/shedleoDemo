package com.shedleo.common.constant;

public final class RedisConstant {

    private RedisConstant() {
    }

    public static final String INVENTORY_STOCK_KEY = "inventory:stock:";
    public static final String INVENTORY_LOCK_KEY = "inventory:lock:";
    public static final String ORDER_NO_KEY = "order:no:";

    public static final long LOCK_WAIT_TIME = 3L;
    public static final long LOCK_LEASE_TIME = 10L;
}

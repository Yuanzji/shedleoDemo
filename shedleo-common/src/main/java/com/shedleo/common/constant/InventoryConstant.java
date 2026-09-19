package com.shedleo.common.constant;

public final class InventoryConstant {

    private InventoryConstant() {
    }

    public static final int CHANGE_TYPE_DEDUCT = 1;
    public static final int CHANGE_TYPE_ROLLBACK = 2;
    public static final int CHANGE_TYPE_PRESALE = 3;

    public static final int PRESALE_STATUS_PENDING = 0;
    public static final int PRESALE_STATUS_SHIPPED = 1;
    public static final int PRESALE_STATUS_CANCELED = 2;
}

package com.shedleo.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    BUSINESS_ERROR(1000, "业务异常"),
    ORDER_NOT_FOUND(1001, "订单不存在"),
    ORDER_STATUS_ERROR(1002, "订单状态不合法"),
    ORDER_ALREADY_CANCELED(1003, "订单已取消"),
    ORDER_ALREADY_PAID(1004, "订单已支付"),
    ORDER_ALREADY_FINISHED(1005, "订单已完成"),

    INVENTORY_NOT_ENOUGH(2001, "库存不足"),
    INVENTORY_NOT_FOUND(2002, "库存记录不存在"),

    FEIGN_CALL_ERROR(3001, "远程服务调用失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

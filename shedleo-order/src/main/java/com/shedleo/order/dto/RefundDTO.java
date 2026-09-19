package com.shedleo.order.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RefundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    private BigDecimal amount;

    private String reason;
}

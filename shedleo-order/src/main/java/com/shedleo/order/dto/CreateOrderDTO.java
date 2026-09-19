package com.shedleo.order.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @NotNull(message = "商家 ID 不能为空")
    private Long merchantId;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull(message = "SKU ID 不能为空")
        private Long skuId;

        @NotNull(message = "购买数量不能为空")
        private Integer quantity;

        @NotNull(message = "价格不能为空")
        private BigDecimal price;

        private String productName;

        private Boolean presale = false;
    }
}

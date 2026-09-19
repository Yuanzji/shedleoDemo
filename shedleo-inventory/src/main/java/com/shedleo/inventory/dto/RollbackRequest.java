package com.shedleo.inventory.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RollbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;
}

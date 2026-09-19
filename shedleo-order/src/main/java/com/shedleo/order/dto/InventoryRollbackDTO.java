package com.shedleo.order.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InventoryRollbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Integer quantity;

    private Long orderId;
}

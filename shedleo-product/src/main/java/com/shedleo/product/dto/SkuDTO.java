package com.shedleo.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU 信息 DTO —— 模拟
 */
@Data
public class SkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long productId;
    private String productName;
    private String skuName;
    private BigDecimal price;
    private String image;
    private String spec;
}

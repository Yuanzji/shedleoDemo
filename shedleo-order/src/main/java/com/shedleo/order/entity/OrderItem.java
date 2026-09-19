package com.shedleo.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order_item")
public class OrderItem {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long orderId;

    private Long skuId;

    private Integer quantity;

    private BigDecimal price;

    private String productName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

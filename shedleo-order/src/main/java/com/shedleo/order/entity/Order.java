package com.shedleo.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long merchantId;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private String mergeGroupId;

    private LocalDateTime expectedDeliveryTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime confirmTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.shedleo.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_refund")
public class Refund {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long orderId;

    private String refundNo;

    private BigDecimal amount;

    private Integer status;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

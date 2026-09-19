package com.shedleo.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_inventory_log")
public class InventoryLog {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long skuId;

    private Long orderId;

    private Integer changeType;

    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

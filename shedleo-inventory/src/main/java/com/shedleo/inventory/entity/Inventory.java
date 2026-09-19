package com.shedleo.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_inventory")
public class Inventory {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long skuId;

    private Integer stock;

    private Integer safeStock;

    @Version
    private Integer version;

    @TableField
    private Long merchantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

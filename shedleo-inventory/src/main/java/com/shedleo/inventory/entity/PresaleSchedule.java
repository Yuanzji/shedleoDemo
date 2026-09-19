package com.shedleo.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_presale_schedule")
public class PresaleSchedule {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long skuId;

    private Long orderId;

    private LocalDateTime expectedDeliveryTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

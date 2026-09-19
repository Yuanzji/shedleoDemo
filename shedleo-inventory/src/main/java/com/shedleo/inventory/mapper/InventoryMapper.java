package com.shedleo.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shedleo.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("UPDATE t_inventory SET stock = stock - #{quantity}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND stock >= #{quantity} AND version = #{version}")
    int deductWithVersion(@Param("skuId") Long skuId,
                          @Param("quantity") int quantity,
                          @Param("version") int version);

    @Update("UPDATE t_inventory SET stock = stock + #{quantity}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND version = #{version}")
    int rollbackWithVersion(@Param("skuId") Long skuId,
                            @Param("quantity") int quantity,
                            @Param("version") int version);
}

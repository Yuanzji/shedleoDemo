package com.shedleo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shedleo.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Update("UPDATE t_order SET status = #{newStatus}, update_time = NOW() WHERE order_no = #{orderNo} AND status = #{oldStatus}")
    int updateStatusByOrderNo(@Param("orderNo") String orderNo,
                              @Param("oldStatus") int oldStatus,
                              @Param("newStatus") int newStatus);

    @Update("UPDATE t_order SET status = #{newStatus}, cancel_time = NOW(), cancel_reason = #{reason}, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND status = #{oldStatus}")
    int cancelOrder(@Param("orderNo") String orderNo,
                    @Param("oldStatus") int oldStatus,
                    @Param("newStatus") int newStatus,
                    @Param("reason") String reason);
}

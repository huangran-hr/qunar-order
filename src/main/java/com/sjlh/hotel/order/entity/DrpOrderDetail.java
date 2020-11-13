package com.sjlh.hotel.order.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分销订单明细表
 */
@Entity
@Table(name = "drp_order_detail")
@Data
public class DrpOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 订单id
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 日期
     */
    @Column(name = "date")
    private LocalDate date;

    /**
     * 售价金额
     */
    @Column(name = "sell_price")
    private Double sellPrice;

    /**
     * 底价金额
     */
    @Column(name = "floor_price")
    private Double floorPrice;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

}

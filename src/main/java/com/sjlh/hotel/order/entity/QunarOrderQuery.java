package com.sjlh.hotel.order.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 去哪儿订单查询表
 */
@Table(name = "qunar_order_query")
@Entity
@Data
public class QunarOrderQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "from_date_time", nullable = false)
    private LocalDateTime fromDateTime;

    @Column(name = "to_date_time", nullable = false)
    private LocalDateTime toDateTime;

    /**
     * 版本
     */
    @Column(name = "version", nullable = false)
    private String version;

}

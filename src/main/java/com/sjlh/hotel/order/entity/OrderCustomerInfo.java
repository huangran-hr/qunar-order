package com.sjlh.hotel.order.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单入住人信息表
 */
@Entity
@Data
@Table(name = "order_customer_info")
public class OrderCustomerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 订单id
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 客户名
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * 客户名（英文）
     */
    @Column(name = "first_name_en")
    private String firstNameEn;

    /**
     * 客户姓
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * 客户姓（英文）
     */
    @Column(name = "last_name_en")
    private String lastNameEn;

    /**
     * 客户姓名全称
     */
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "create_time")
    private LocalDateTime createTime;

}

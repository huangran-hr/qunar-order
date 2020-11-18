package com.sjlh.hotel.order.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分销订单表
 */
@Entity
@Table(name = "drp_order")
@Data
public class DrpOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 订单号
     */
    @Column(name = "order_no", nullable = false)
    private String orderNo;

    /**
     * OTA订单号，由OTA传过来
     */
    @Column(name = "ota_order_no")
    private String otaOrderNo;

    /**
     * 支付方式  CREDIT_PAY:授信额度
     */
    @Column(name = "pay_type")
    private String payType;

    /**
     * 预定房间数
     */
    @Column(name = "room_num")
    private Integer roomNum;

    /**
     * 订单总金额
     */
    @Column(name = "total_money")
    private Double totalMoney;

    /**
     * 支付总金额
     */
    @Column(name = "pay_money")
    private Double payMoney;

    /**
     * 订单底价金额
     */
    @Column(name = "order_floor_money")
    private Double orderFloorMoney;

    /**
     * 酒店代码
     */
    @Column(name = "hotel_code")
    private String hotelCode;

    /**
     * 酒店类型 0：红树林系列酒店 1：直签酒店 2：泰坦云酒店
     */
    @Column(name = "hotel_type")
    private Integer hotelType;

    /**
     * 酒店名称
     */
    @Column(name = "hotel_name")
    private String hotelName;

    /**
     * 房型代码
     */
    @Column(name = "room_type_code")
    private String roomTypeCode;

    /**
     * 价格计划code
     */
    @Column(name = "rate_plan_code")
    private String ratePlanCode;

    @Column(name = "room_type_name")
    private String roomTypeName;

    /**
     * 产品名称
     */
    @Column(name = "product_name")
    private String productName;

    /**
     * 入住日期
     */
    @Column(name = "checkin_date")
    private LocalDate checkinDate;

    /**
     * 离店日期
     */
    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    /**
     * 现预付类型 0：预付 1：现付
     */
    @Column(name = "cash_advance_type")
    private Integer cashAdvanceType;

    /**
     * 支付流水号
     */
    @Column(name = "pay_no")
    private String payNo;

    /**
     * crs订单id
     */
    @Column(name = "crs_order_id")
    private String crsOrderId;

    /**
     * 确认号（多个用英文逗号隔开）
     */
    @Column(name = "confirm_no")
    private String confirmNo;

    /**
     * pms确认号
     */
    @Column(name = "pms_confirm_no")
    private String pmsConfirmNo;

    /**
     * 订单状态 1：待确认 2：已接单 3：拒单中 4：已拒单 5：已完成 6：已取消 7：失败
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 渠道code
     */
    @Column(name = "channel_code")
    private String channelCode;

    /**
     * 联系人姓名
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

}

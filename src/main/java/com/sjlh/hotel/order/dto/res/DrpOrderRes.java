package com.sjlh.hotel.order.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.errorprone.annotations.FormatString;
import lombok.Data;
import org.checkerframework.checker.formatter.qual.Format;

/**
 * @Auther: HR
 * @Date 2020/11/27 16:00
 * @Description:
 */
@Data
public class DrpOrderRes {
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * OTA订单号，由OTA传过来
     */
    private String otaOrderNo;

    /**
     * 支付方式  CREDIT_PAY:授信额度
     */
    private String payType;

    /**
     * 预定房间数
     */
    private Integer roomCount;

    /**
     * 订单总金额
     */
    private Double totalMoney;

    /**
     * 支付总金额
     */
    private Double payMoney;

    /**
     * 订单底价金额
     */
    private Double orderFloorMoney;

    /**
     * 酒店代码
     */
    private String hotelCode;

    /**
     * 酒店类型 0：红树林系列酒店 1：直签酒店 2：泰坦云酒店
     */
    private Integer hotelType;

    /**
     * 酒店名称
     */
    private String hotelName;

    /**
     * 房型代码
     */
    private String roomTypeCode;

    /**
     * 价格计划code
     */
    private String ratePlanCode;

    private String roomTypeName;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 入住日期
     */
    private String checkinDate;

    /**
     * 离店日期
     */
    private String checkoutDate;

    /**
     * 现预付类型 0：预付 1：现付
     */
    private Integer cashAdvanceType;

    /**
     * 支付流水号
     */
    private String payNo;

    /**
     * crs订单id
     */
    private String crsOrderId;

    /**
     * 确认号（多个用英文逗号隔开）
     */
    private String confirmNo;

    /**
     * pms确认号
     */
    private String pmsConfirmNo;

    /**
     * 订单状态 1：待确认 2：已接单 3：拒单中 4：已拒单 5：已完成 6：已取消 7：失败
     */
    private Integer status;

    /**
     * 入住状态  0：未入住  1：已入住
     */
    private Integer checkinStatus;

    /**
     * 渠道code
     */
    private String channelCode;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}

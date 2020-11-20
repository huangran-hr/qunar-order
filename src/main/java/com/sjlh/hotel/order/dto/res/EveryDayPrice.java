package com.sjlh.hotel.order.dto.res;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Auther: HR
 * @Date 2020/11/18 18:05
 * @Description:
 */
@Data
public class EveryDayPrice {
    private String date;
    private Integer roomStatus; //房型状态 1为关房，0为开房
    private BigDecimal price; //当日价格（卖价）
    private BigDecimal deposit; //预付定金金额
    private BigDecimal priceCut; //直减金额，对应报价接口的discountAmount
    private BigDecimal basePrice;//当日价格（底价）
}

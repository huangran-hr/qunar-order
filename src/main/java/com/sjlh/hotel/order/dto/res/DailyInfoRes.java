package com.sjlh.hotel.order.dto.res;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Auther: HR
 * @Date 2020/11/23 14:53
 * @Description:
 */
@Data
public class DailyInfoRes {
    /**
     * 房态
     */
    private Boolean available;

    /**
     * 房量
     */
    private Long roomCount;

    /**
     * 售价
     */
    private BigDecimal sellerPrice;

    /**
     * 底价
     */
    private BigDecimal basePrice;

}

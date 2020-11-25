package com.sjlh.hotel.order.dto.req;

import lombok.Data;

/**
 * @Auther: HR
 * @Date 2020/11/23 14:29
 * @Description:
 */
@Data
public class ProductReq {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 入住日期，yyyy-MM-dd
     */
    private String formDate;

    /**
     * 离店日期，yyyy-MM-dd
     */
    private String toDate;
}

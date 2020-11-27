package com.sjlh.hotel.order.dto.req;

import lombok.Data;

/**
 * @Auther: HR
 * @Date 2020/11/27 14:51
 * @Description:
 */
@Data
public class DrpOrderReq {

    private String orderNo;

    private String otaOrderNo;

    private Integer status;

    private String hotelName;

    private String createStartTime;

    private String createEndTime;

    private Integer pageNo;

    private Integer pageSize;
}

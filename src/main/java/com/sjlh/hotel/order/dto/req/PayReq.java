package com.sjlh.hotel.order.dto.req;

import lombok.Data;

@Data
public class PayReq {
  private Integer orderId;
  private String orderSn;

  private double money;
}

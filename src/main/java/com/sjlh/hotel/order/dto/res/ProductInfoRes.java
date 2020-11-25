package com.sjlh.hotel.order.dto.res;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class ProductInfoRes {
  private String pmsHotelCode;
  private String roomTypeCode;
  private String rateCode;
  private String productName;
  private Map<LocalDate,DailyInfoRes> dailyInfo;
}

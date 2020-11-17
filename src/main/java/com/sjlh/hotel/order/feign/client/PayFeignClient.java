package com.sjlh.hotel.order.feign.client;

import com.sjlh.hotel.order.dto.req.PayReq;
import com.sjlh.hotel.order.dto.res.SimpleRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: HR
 * @Date 2020/11/17 10:41
 * @Description:
 */
@FeignClient(name="qunar-service")
public interface PayFeignClient {
    /**
     * 授信支付
     * @param PayReq
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value="/pay/pay")
    SimpleRes pay(@RequestBody PayReq PayReq);
}

package com.sjlh.hotel.order.feign.client;

import com.sjlh.hotel.order.dto.req.PayReq;
import com.sjlh.hotel.order.dto.res.ProductInfoRes;
import com.sjlh.hotel.order.dto.res.SimpleRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Auther: HR
 * @Date 2020/11/17 10:41
 * @Description:
 */
@FeignClient(name="qunar-service")
public interface QunarServiceFeignClient {
    /**
     * 获取产品信息
     * @param productId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value="/product/info/{productId}")
    ProductInfoRes getProductInfo(@PathVariable String productId);

    /**
     * 授信支付
     * @param PayReq
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value="/pay/pay")
    SimpleRes pay(@RequestBody PayReq PayReq);
}
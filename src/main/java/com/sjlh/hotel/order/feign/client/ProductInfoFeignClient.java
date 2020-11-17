package com.sjlh.hotel.order.feign.client;

import com.sjlh.hotel.order.dto.res.ProductInfoRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Auther: HR
 * @Date 2020/11/17 10:41
 * @Description:
 */
@FeignClient(name="qunar-service")
public interface ProductInfoFeignClient {
    /**
     * 获取产品信息
     * @param productId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value="/product/info/{productId}")
    ProductInfoRes getInfo(@PathVariable String productId);
}

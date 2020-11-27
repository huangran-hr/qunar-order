package com.sjlh.hotel.order.controller;

import com.sjlh.hotel.order.dto.req.DrpOrderReq;
import com.sjlh.hotel.order.dto.res.DrpOrderRes;
import com.sjlh.hotel.order.dto.res.SimpleRes;
import com.sjlh.hotel.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @Auther: HR
 * @Date: 2020/11/5 14:47
 * @Description:
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 同步去哪儿订单，存放到消息中
     * @return
     */
    @RequestMapping(value = "orders")
    public String getOrders(){
        String code = "SUCCESS";
        try {
            orderService.getOrders();
        }catch (Exception e){
            code = "FAIL";
            e.printStackTrace();
        }
        return code;
    }


    /**
     * 从消息中取出数据，并创建订单
     */
//    @RequestMapping(value = "order/create")
//    public String createOrders(){
//        orderService.createOrders();
//        return "SUCCESS";
//    }

    /**
     * 获取前一天的 已接单订单，并将订单操作类型推送给qunar
     * @return
     */
    @RequestMapping(value = "order/opt/push")
    public String optOrderPush(){
        orderService.optOrderPush();
        return "SUCCESS";
    }


    /**
     * 查询订单列表
     * @param drpOrderReq
     * @return
     */
    @RequestMapping(value = "order/list", method = RequestMethod.POST)
    @ResponseBody
    public List<DrpOrderRes> queryDrpOrderList(@RequestBody DrpOrderReq drpOrderReq){

        List<DrpOrderRes> DrpOrderList = orderService.queryDrpOrderList(drpOrderReq);

        return DrpOrderList;
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @RequestMapping(value = "order/cancel/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleRes cancelOrder(@PathVariable String orderId){
        SimpleRes simpleRes = new SimpleRes();
        simpleRes.setCode(200);
        simpleRes.setMessage("取消成功！");
        try {
            orderService.cancelOrder(orderId);
        } catch (Exception e){
            simpleRes.setCode(201);
            simpleRes.setMessage("取消失败！");
            e.printStackTrace();
        }
        return simpleRes;
    }
}

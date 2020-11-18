package com.sjlh.hotel.order.job;

import com.sjlh.hotel.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @Auther: HR
 * @Date 2020/11/18 10:18
 * @Description:
 */
@Service
public class OrderJob {

    private static final Logger logger = LoggerFactory.getLogger(OrderJob.class);

    @Autowired
    private OrderService orderService;

    /**
     * 同步qunar订单（每5分钟执行一次）
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void syncOrder(){
        logger.info("同步qunar订单开始==============");
        orderService.getOrders();
        logger.info("同步qunar订单结束==============");
    }

    /**
     * 同步qunar订单（每6分钟执行一次）
     */
    @Scheduled(cron = "0 0/6 * * * ? ")
    public void createOrders(){
        logger.info("创建订单开始==============");
        orderService.createOrders();
        logger.info("创建订单结束==============");
    }

    /**
     * 订单操作推送（每天5点执行一次）
     */
    @Scheduled(cron = "0 0 5 * * ? ")
    public void optOrderPush(){
        logger.info("订单操作同步开始==============");
        orderService.optOrderPush();
        logger.info("订单操作同步结束==============");
    }
}

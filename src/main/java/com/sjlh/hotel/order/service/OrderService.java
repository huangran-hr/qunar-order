package com.sjlh.hotel.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjlh.hotel.crs.core.CrsOrderService;
import com.sjlh.hotel.crs.model.*;
import com.sjlh.hotel.order.core.ResStatus;
import com.sjlh.hotel.order.dto.req.PayReq;
import com.sjlh.hotel.order.dto.res.ProductInfoRes;
import com.sjlh.hotel.order.dto.res.SimpleRes;
import com.sjlh.hotel.order.entity.DrpOrder;
import com.sjlh.hotel.order.entity.DrpOrderDetail;
import com.sjlh.hotel.order.entity.OrderCustomerInfo;
import com.sjlh.hotel.order.feign.client.PayFeignClient;
import com.sjlh.hotel.order.feign.client.ProductInfoFeignClient;
import com.sjlh.hotel.order.repository.DrpOrderDetailRepository;
import com.sjlh.hotel.order.repository.DrpOrderRepository;
import com.sjlh.hotel.order.repository.OrderCustomerInfoRepository;
import com.sjlh.hotel.qunar.core.ArrangeType;
import com.sjlh.hotel.qunar.core.OrderOpt;
import com.sjlh.hotel.qunar.core.QunarService;
import com.sjlh.hotel.qunar.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Auther: HR
 * @Date: 2020/11/5 14:57
 * @Description:
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private QunarService qunarService;

    @Autowired
    private DrpOrderRepository drpOrderRepository;

    @Autowired
    private DrpOrderDetailRepository drpOrderDetailRepository;

    @Autowired
    private OrderCustomerInfoRepository orderCustomerInfoRepository;

    @Autowired
    private CrsOrderService crsOrderService;

    @Autowired
    private ProductInfoFeignClient productInfoFeignClient;

    @Autowired
    private PayFeignClient payFeignClient;

    public final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 同步qunar订单，放在消息中
     */
    public void getOrders(){
        try {
            QueryOrderRequestDto queryOrderRequestDto = new QueryOrderRequestDto();
//            queryOrderRequestDto.setFromDate();
//            queryOrderRequestDto.setToDate();
//            queryOrderRequestDto.setVersion();
            //获取去哪儿订单
            QueryOrderResponseDto queryOrderResponseDto = qunarService.queryOrderList(queryOrderRequestDto);

            if(queryOrderResponseDto != null && queryOrderResponseDto.getRet()){
                List<OrderInfoResponseDto> qunarOrderInfoDtos = queryOrderResponseDto.getData();
                //获取已确认状态的订单
                qunarOrderInfoDtos.stream().filter(q -> q.getStatusCode()==5);
            }

        }catch (Exception e){
            logger.error("同步去哪儿订单失败！");
            throw e;
        }

    }

    /**
     * 从消息中取出数据，并创建订单
     */
    public OrderCreateRsp createOrder() throws JsonProcessingException {

        OrderCreateRsp crsRoomOrderRsp = new OrderCreateRsp();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //查询产品信息
        ProductInfoRes productInfo = productInfoFeignClient.getInfo("4819");

        DrpOrder order = new DrpOrder();
        order.setStatus(2); //已接单
        order.setCashAdvanceType(0); //预付
        order.setChannelCode("qunar");
        order.setCheckinDate(LocalDate.now());
        order.setCheckoutDate(LocalDate.now().plusDays(1));

        order.setCreateTime(LocalDateTime.now());
        order.setHotelCode(productInfo.getPmsHotelCode());
        order.setRatePlanCode(productInfo.getRateCode());
        order.setRoomTypeCode(productInfo.getRoomTypeCode());
        order.setHotelName("三亚湾红树林酒店");
        order.setProductName("大王棕豪华园景房");
        order.setHotelType(0);//酒店类型 0：红树林系列酒店
        order.setOrderNo("YD123456789");
        order.setOtaOrderNo("D123456789");
        order.setRoomNum(1);
        order.setOrderFloorMoney(Double.valueOf(400));
        order.setPayMoney(Double.valueOf(500));
        order.setTotalMoney(Double.valueOf(500));

        order.setContactName("测试"); //联系人
        order.setPhone("13001108111");
        order.setPayType("CREDIT_PAY");

        order.setRemark("ceshi");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        //保存订单
        drpOrderRepository.save(order);

        DrpOrderDetail drpOrderDetail = new DrpOrderDetail();
        //保存订单明细
        drpOrderDetailRepository.save(drpOrderDetail);

        OrderCustomerInfo orderCustomerInfo = new OrderCustomerInfo();
        //保存订单入住人信息
        orderCustomerInfoRepository.save(orderCustomerInfo);

        LocalDate checkinDate = order.getCheckinDate(); //入住日期
        LocalDate checkoutDate = order.getCheckoutDate();//离店日期

        ZonedDateTime checkinZonedDateTime = checkinDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime checkoutZonedDateTime = checkoutDate.atStartOfDay(ZoneId.systemDefault());

        OrderCreateReq crsRoomOrderReq = new OrderCreateReq();
        // crs订单创建
        crsRoomOrderReq.setOrderNo(order.getOrderNo());
        crsRoomOrderReq.setOtaOrderNo(order.getOtaOrderNo());
        crsRoomOrderReq.setPmsHotelCode(order.getHotelCode());
        crsRoomOrderReq.setRateCode(order.getRatePlanCode());
        crsRoomOrderReq.setRoomCount(order.getRoomNum());
        crsRoomOrderReq.setRoomTypeCode(order.getRoomTypeCode());
        crsRoomOrderReq.setTotalPrice(order.getTotalMoney());
        crsRoomOrderReq.setCheckInDate(Date.from(checkinZonedDateTime.toInstant()));
        crsRoomOrderReq.setCheckOutDate(Date.from(checkoutZonedDateTime.toInstant()));
        crsRoomOrderReq.setProductName(order.getProductName());
        crsRoomOrderReq.setMobile(order.getPhone());

        List<OrderGuest> crsGuests = new ArrayList<OrderGuest>();
        OrderGuest orderGuest =  new OrderGuest();
        orderGuest.setFirstName("试");
        orderGuest.setLastName("测");
        orderGuest.setName(order.getContactName());
        crsGuests.add(orderGuest);
        crsRoomOrderReq.setGuests(crsGuests);

        //crs价格明细
//        List<OrderDayPrice> crsDayPriceList = new ArrayList<>();
//        OrderDayPrice orderDayPrice = new OrderDayPrice();
//        orderDayPrice.setDate(sdf.parse("2020-11-09"));
//        orderDayPrice.setPrice(Double.valueOf(500));
//        crsDayPriceList.add(orderDayPrice);
//        crsRoomOrderReq.setDayPrices(crsDayPriceList);

        //mvm 不需要以下信息（mvm-drp-crs 有默认值）
//        crsRoomOrderReq.setAccountOfTravelAgency("MWEB");
//        crsRoomOrderReq.setTravelAgency("N1");
//        crsRoomOrderReq.setSource("MWEB");
//        crsRoomOrderReq.setMarket("IND");
//        crsRoomOrderReq.setGuesttypeCode("0000");

        //计算晚数
        Period period = checkinDate.until(checkoutDate);
        long days = period.getDays();

        //comments 中预定明细
//        orderItemPriceList.forEach(p->{
//            priceInfo.append(LocalDateUtil.toLocalDate(p.getReservDate()));
//            priceInfo.append("(" + new BigDecimal(p.getTotalAmount()).setScale(2, BigDecimal.ROUND_HALF_UP) + ")  ");
//        });

        StringBuilder sb = new StringBuilder();
        sb.append("房型产品名称：" + order.getProductName() + "\r\n");
        sb.append("客房订单号：" + order.getOrderNo() + "\r\n");
        sb.append("预订时间：" + order.getCheckinDate().format(fmt) + "-" + order.getCheckoutDate().format(fmt) + " 预订" + days + "晚 共计" + order.getTotalMoney() + "元" + "\r\n");
        sb.append("预订明细：2020.09.27(710.0) " + order.getOrderNo() + "\r\n");
        String remark = "无";
        sb.append("用户需求：" + remark + "\r\n");
        sb.append("支付状态：已支付\r\n");
        sb.append("支付方式：授信额度\r\n");
        sb.append("------来源：MVM猫喂猫云平台------");
        crsRoomOrderReq.setRemarks(sb.toString());
        //使用crsClient， crs创建订单
        logger.info("调用mvm-drp-crs创建订单，请求参数crsRoomOrderReq===" + objectMapper.writeValueAsString(crsRoomOrderReq));
        crsRoomOrderRsp = crsOrderService.create(crsRoomOrderReq);
        logger.info("调用mvm-drp-crs创建订单，响应参数crsRoomOrderRsp===" + objectMapper.writeValueAsString(crsRoomOrderRsp));

        OrderOpt opt = OrderOpt.ARRANGE_ROOM;  //安排房间
        Float money = Float.valueOf(order.getPayMoney().toString());
        //CRS 创建订单
        if (null != crsRoomOrderRsp && crsRoomOrderRsp.getCode().equals(ResultCode.OK.getValue())){ //成功
            logger.info("CRS创建订单成功===========");
            String crsOrderId = crsRoomOrderRsp.getCrs_order_id();
            order.setCrsOrderId(crsOrderId);
            StringBuffer confirmNo = new StringBuffer();
            confirmNo.append(crsOrderId);
            if (null != crsRoomOrderRsp.getSplitOrderIds()) {
                confirmNo.append(",");
                confirmNo.append(crsRoomOrderRsp.getSplitOrderIds());
            }
            order.setConfirmNo(confirmNo.toString());

            //支付
            toPay(order);

        }else{
            logger.info("CRS创建订单失败===========");
            opt= OrderOpt.APPLY_UNSUBSCRIBE;  //申请退订
            order.setStatus(4); //已拒单
            order.setUpdateTime(LocalDateTime.now());
        }

        if(order.getId() != null){
            //更新订单状态
            drpOrderRepository.save(order);
        }

        //订单操作类型推送给qunar
        optOrder(order.getOtaOrderNo(),opt,money);

        return crsRoomOrderRsp;
    }

    /**
     * 支付
     * @param order
     * @throws JsonProcessingException
     */
    private void toPay(DrpOrder order) throws JsonProcessingException {
        PayReq payReq = new PayReq();
        payReq.setOrderId(Integer.decode(order.getId().toString()));
        payReq.setOrderSn(order.getOrderNo());
        payReq.setMoney(order.getPayMoney());
        //调用授信支付接口
        logger.info("调用授信支付，请求参数payReq===" + objectMapper.writeValueAsString(payReq));
        SimpleRes simpleRes =  payFeignClient.pay(payReq);
        logger.info("调用授信支付，响应参数simpleRes===" + objectMapper.writeValueAsString(simpleRes));
        //支付失败
        if(simpleRes == null || simpleRes.getCode() != 200){
            logger.error("授信支付失败！");
            order.setStatus(7); //7：失败
            order.setUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 获取前一天的 已接单订单，并将订单操作类型推送给qunar
     */
    public void optOrderPush(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInDate = LocalDate.now().plusDays(-1).format(fmt);
        List<DrpOrder> orders = drpOrderRepository.findByCheckinDate(checkInDate);
        orders.forEach(o -> {
            OrderOpt opt = OrderOpt.ARRANGE_ROOM;  //安排房间
            OrderDetailReq orderDetailReq = new OrderDetailReq();
            orderDetailReq.setCrsOrderId(Integer.decode(o.getCrsOrderId()));
            //调用crs接口获取订单信息
            OrderDetailRsp orderDetailRsp = crsOrderService.detail(orderDetailReq);
            if(orderDetailRsp != null && orderDetailRsp.getStatusCode() != null ){
                String statusCode = orderDetailRsp.getStatusCode();
                if(ResStatus.NOSHOW.getStatus().equals(statusCode)) {  //未入住
                    opt = OrderOpt.CONFIRM_NOSHOW; //确认未入住
                } else if(ResStatus.CHECKIN.getStatus().equals(statusCode)){ //入住
                    opt = OrderOpt.CONFIRM_SHOW;  //确认入住
                }
            }
            //订单操作类型推送给qunar
            try {
                optOrder(o.getOtaOrderNo(),opt,null);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 订单操作类型推送给qunar
     * @param otaOrderNum  去哪儿订单号
     * @param opt  订单操作类型
     * @param money 用户支付金额 申请退订时才有值
     * @return
     */
    public OptOrderRequestDto optOrder(String otaOrderNum, OrderOpt opt,Float money) throws JsonProcessingException {
        OptOrderRequestDto optOrderRequestDto = new OptOrderRequestDto();
        optOrderRequestDto.setOrderNum(otaOrderNum);
        optOrderRequestDto.setOpt(opt);
        if(OrderOpt.ARRANGE_ROOM.equals(opt)){   //安排房间
            optOrderRequestDto.setArrangeType(ArrangeType.NAME);
        } else if(OrderOpt.APPLY_UNSUBSCRIBE.equals(opt)){ //申请退订
            optOrderRequestDto.setMoney(money);
        }
        //调用qunar 订单操作接口
        logger.info("调用qunar订单操作接口，请求参数optOrderRequestDto===" + objectMapper.writeValueAsString(optOrderRequestDto));
        OptOrderResponseDto optOrderResponseDto = qunarService.optOrder(optOrderRequestDto);
        logger.info("调用qunar订单操作接口，响应参数optOrderResponseDto===" + objectMapper.writeValueAsString(optOrderResponseDto));
        return optOrderRequestDto;
    }
}

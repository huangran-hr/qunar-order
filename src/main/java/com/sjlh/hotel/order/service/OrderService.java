package com.sjlh.hotel.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sjlh.hotel.crs.core.CrsOrderService;
import com.sjlh.hotel.crs.model.*;
import com.sjlh.hotel.order.core.ResStatus;
import com.sjlh.hotel.order.dto.req.DrpOrderReq;
import com.sjlh.hotel.order.dto.req.PayReq;
import com.sjlh.hotel.order.dto.req.ProductReq;
import com.sjlh.hotel.order.dto.res.*;
import com.sjlh.hotel.order.entity.DrpOrder;
import com.sjlh.hotel.order.entity.DrpOrderDetail;
import com.sjlh.hotel.order.entity.OrderCustomerInfo;
import com.sjlh.hotel.order.entity.QunarOrderQuery;
import com.sjlh.hotel.order.feign.client.QunarServiceFeignClient;
import com.sjlh.hotel.order.kafka.service.KafkaProducerService;
import com.sjlh.hotel.order.repository.DrpOrderDetailRepository;
import com.sjlh.hotel.order.repository.DrpOrderRepository;
import com.sjlh.hotel.order.repository.OrderCustomerInfoRepository;
import com.sjlh.hotel.order.repository.QunarOrderQueryRepository;
import com.sjlh.hotel.qunar.core.ArrangeType;
import com.sjlh.hotel.qunar.core.OrderOpt;
import com.sjlh.hotel.qunar.core.QunarService;
import com.sjlh.hotel.qunar.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

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
    private QunarServiceFeignClient qunarServiceFeignClient;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private QunarOrderQueryRepository qunarOrderQueryRepository;

    public final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取8分钟内的订单从Qunar同步到自己的系统中，放在消息中
     */
    public void getOrders() {
        try {
            QunarOrderQuery qunarOrderQuery = qunarOrderQueryRepository.findById(1).get();
            if(LocalDateTime.now().compareTo(qunarOrderQuery.getToDateTime())<0){
                return;
            }
            QueryOrderRequestDto queryOrderRequestDto = new QueryOrderRequestDto();
            queryOrderRequestDto.setFromDate(qunarOrderQuery.getFromDateTime());
            queryOrderRequestDto.setToDate(qunarOrderQuery.getToDateTime());
            queryOrderRequestDto.setVersion(qunarOrderQuery.getVersion());
            //获取去哪儿订单
            logger.info("调用去哪儿查询订单，请求参数queryOrderRequestDto===" + objectMapper.writeValueAsString(queryOrderRequestDto));
            QueryOrderResponseDto queryOrderResponseDto = qunarService.queryOrderList(queryOrderRequestDto);
            logger.info("调用去哪儿查询订单，请求参数queryOrderResponseDto===" + objectMapper.writeValueAsString(queryOrderResponseDto));

            if (queryOrderResponseDto != null && queryOrderResponseDto.getRet()) {
                if(queryOrderResponseDto.getData().size()>0){
                    List<OrderInfoResponseDto> qunarOrderInfoDtos = queryOrderResponseDto.getData();
                    //获取已确认状态的订单
                    qunarOrderInfoDtos.stream().filter(q -> q.getStatusCode() == 5).collect(Collectors.toList());

                    //发送数据到消息队列
                    logger.info("发送数据到消息队列qunarOrders，qunarOrderInfoDtos===" + objectMapper.writeValueAsString(qunarOrderInfoDtos));
                    kafkaProducerService.sendMessageSync("qunarOrders", qunarOrderInfoDtos);
                }

                qunarOrderQuery.setFromDateTime(qunarOrderQuery.getToDateTime().plusSeconds(-1));
                qunarOrderQuery.setToDateTime(qunarOrderQuery.getToDateTime().plusMinutes(8)); //指定查询8分钟的订单
                //更新
                qunarOrderQueryRepository.save(qunarOrderQuery);
            }

        } catch (Exception e) {
            logger.error("同步去哪儿订单失败！");
            e.printStackTrace();
        }

    }

    /**
     * 从消息中取出数据，并创建订单
     */
    @KafkaListener(topics = {"qunarOrders"}, groupId = "group2", containerFactory="kafkaListenerContainerFactory")
    public void createOrders(String message) throws JsonProcessingException {
        List<OrderInfoResponseDto> qunarOrderInfoDtos = objectMapper.readValue(message,TypeFactory.defaultInstance().constructCollectionType(List.class, OrderInfoResponseDto.class));
        for (OrderInfoResponseDto orderInfoResponseDto : qunarOrderInfoDtos) {
            createOrder(orderInfoResponseDto);
        }
    }

    /**
     * 创建订单
     */
    public void createOrder(OrderInfoResponseDto orderInfoResponseDto) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {

            //根据otaOrderNo查询订单
            DrpOrder d = drpOrderRepository.findByOtaOrderNo(orderInfoResponseDto.getOrderNum());
            if(d != null) {
                logger.info("此订单已存在！otaOrderNo:" + orderInfoResponseDto.getOrderNum());
                return;
            }

            ProductReq productReq = new ProductReq();
            productReq.setProductId("4819");
            productReq.setFormDate(orderInfoResponseDto.getCheckInDate().toString());
            productReq.setToDate(orderInfoResponseDto.getCheckOutDate().toString());
            //查询产品信息
            ProductInfoRes productInfo = qunarServiceFeignClient.getProductInfo(productReq);

            String everyDayPriceStr = orderInfoResponseDto.getEveryDayPrice(); //每日价格列表
            List<EveryDayPrice> everyDayPrices = objectMapper.readValue(everyDayPriceStr, TypeFactory.defaultInstance().constructCollectionType(List.class, EveryDayPrice.class));

            Map<LocalDate,DailyInfoRes> dailyInfoResMap = productInfo.getDailyInfo();
            //校验
            boolean flag = check(everyDayPrices,dailyInfoResMap);
            if(!flag){   //校验失败
                logger.info("校验失败！");
                return;
            }

            BigDecimal totalBasePrice = BigDecimal.ZERO;
            for (LocalDate date : dailyInfoResMap.keySet()) {
                totalBasePrice =  totalBasePrice.add(dailyInfoResMap.get(date).getBasePrice());
            }

            DrpOrder order = new DrpOrder();
            order.setStatus(2); //已接单
            order.setCashAdvanceType(0); //预付
            order.setChannelCode("Qunar");

            order.setCheckinDate(LocalDate.parse(orderInfoResponseDto.getCheckInDate().toString(), fmt2));
            order.setCheckoutDate(LocalDate.parse(orderInfoResponseDto.getCheckOutDate().toString(), fmt2));
            LocalDateTime currLocalDateTime = LocalDateTime.now();
            order.setCreateTime(currLocalDateTime);
            order.setHotelCode(productInfo.getPmsHotelCode());
            order.setRatePlanCode(productInfo.getRateCode());
            order.setRoomTypeCode(productInfo.getRoomTypeCode());
            order.setHotelName(orderInfoResponseDto.getHotelName());
            order.setProductName(productInfo.getProductName());
            order.setHotelType(0);//酒店类型 0：红树林系列酒店
            //订单号规则：FYDKF年月日时分+6位随机数字
            order.setOrderNo("FYDKF" + currLocalDateTime.getYear() + currLocalDateTime.getMonthValue() + currLocalDateTime.getDayOfMonth()
                    + currLocalDateTime.getHour() + currLocalDateTime.getMinute() + currLocalDateTime.getSecond() + getRandom(6));
            order.setOtaOrderNo(orderInfoResponseDto.getOrderNum());
            order.setRoomCount(orderInfoResponseDto.getRoomNum());

            order.setOrderFloorMoney(totalBasePrice.doubleValue());
            order.setPayMoney(orderInfoResponseDto.getTotalBasePrice().doubleValue());
            order.setTotalMoney(orderInfoResponseDto.getTotalBasePrice().doubleValue());

            order.setContactName(orderInfoResponseDto.getContactName()); //联系人
            order.setPhone(orderInfoResponseDto.getContactPhone()); //联系人手机号
            order.setPayType("CREDIT_PAY");

            String request = orderInfoResponseDto.getRequest(); //特殊要求
            order.setRemark(request);

            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            //保存订单
            drpOrderRepository.save(order);

            //crs入参
            OrderCreateReq crsRoomOrderReq = new OrderCreateReq();
            //crs客户信息
            List<OrderGuest> crsGuests = new ArrayList<OrderGuest>();
            //crs价格明细列表
            List<OrderDayPrice> crsDayPriceList = new ArrayList<>();
            StringBuilder priceInfo = new StringBuilder();

            everyDayPrices.forEach(e -> {
                DrpOrderDetail drpOrderDetail = new DrpOrderDetail();
                drpOrderDetail.setOrderId(order.getId());
                drpOrderDetail.setDate(LocalDate.parse(e.getDate()));
                drpOrderDetail.setSellPrice(e.getPrice().doubleValue());
                drpOrderDetail.setFloorPrice(e.getBasePrice().doubleValue());
                drpOrderDetail.setCreateTime(LocalDateTime.now());
                drpOrderDetail.setUpdateTime(LocalDateTime.now());

                //保存订单明细
                drpOrderDetailRepository.save(drpOrderDetail);

                DailyInfoRes dailyInfoRes = dailyInfoResMap.get(LocalDate.parse(e.getDate()));

                //crs价格明细
                OrderDayPrice orderDayPrice = new OrderDayPrice();
                orderDayPrice.setDate(Date.from(LocalDate.parse(e.getDate()).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                orderDayPrice.setPrice(dailyInfoRes.getBasePrice().doubleValue());
                crsDayPriceList.add(orderDayPrice);
                crsRoomOrderReq.setDayPrices(crsDayPriceList);

                //comments 中预定明细
                priceInfo.append(e.getDate());
                priceInfo.append("(" + dailyInfoRes.getBasePrice() + ")  ");
            });


            String customerNameStr = orderInfoResponseDto.getCustomerName(); //入住人
            String[] customerNames = customerNameStr.split("|");
            for (String customerName : customerNames) {
                OrderCustomerInfo orderCustomerInfo = new OrderCustomerInfo();
                orderCustomerInfo.setOrderId(order.getId());
                orderCustomerInfo.setLastName(customerName.substring(0, 1));
                orderCustomerInfo.setFirstName(customerName.substring(1));
                orderCustomerInfo.setFullName(customerName);
                //保存订单入住人信息
                orderCustomerInfoRepository.save(orderCustomerInfo);

                //crs客户信息
                OrderGuest orderGuest = new OrderGuest();
                orderGuest.setFirstName(orderCustomerInfo.getFirstName());
                orderGuest.setLastName(orderCustomerInfo.getLastName());
                orderGuest.setName(customerName);
                crsGuests.add(orderGuest);
                crsRoomOrderReq.setGuests(crsGuests);
            }

            //授信支付
            toPay(order);

            LocalDate checkinDate = order.getCheckinDate(); //入住日期
            LocalDate checkoutDate = order.getCheckoutDate();//离店日期

            ZonedDateTime checkinZonedDateTime = checkinDate.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime checkoutZonedDateTime = checkoutDate.atStartOfDay(ZoneId.systemDefault());

            // crs订单创建
            crsRoomOrderReq.setOrderNo(order.getOrderNo());
            crsRoomOrderReq.setOtaOrderNo(order.getOtaOrderNo());
            crsRoomOrderReq.setPmsHotelCode(order.getHotelCode());
            crsRoomOrderReq.setRateCode(order.getRatePlanCode());
            crsRoomOrderReq.setRoomCount(order.getRoomCount());
            crsRoomOrderReq.setRoomTypeCode(order.getRoomTypeCode());
            crsRoomOrderReq.setTotalPrice(order.getTotalMoney());
            crsRoomOrderReq.setCheckInDate(Date.from(checkinZonedDateTime.toInstant()));
            crsRoomOrderReq.setCheckOutDate(Date.from(checkoutZonedDateTime.toInstant()));
            crsRoomOrderReq.setProductName(order.getProductName());
            crsRoomOrderReq.setMobile(order.getPhone());

            //mvm 不需要以下信息（mvm-drp-crs 有默认值）
//        crsRoomOrderReq.setAccountOfTravelAgency("MWEB");
//        crsRoomOrderReq.setTravelAgency("N1");
//        crsRoomOrderReq.setSource("MWEB");
//        crsRoomOrderReq.setMarket("IND");
//        crsRoomOrderReq.setGuesttypeCode("0000");

            //计算晚数
            Period period = checkinDate.until(checkoutDate);
            long days = period.getDays();

            StringBuilder sb = new StringBuilder();
            sb.append("房型产品名称：" + order.getProductName() + "\r\n");
            sb.append("客房订单号：" + order.getOrderNo() + "\r\n");
            sb.append("预订时间：" + order.getCheckinDate().format(fmt) + "-" + order.getCheckoutDate().format(fmt) + " 预订" + days + "晚 共计" + order.getTotalMoney() + "元" + "\r\n");
            sb.append("预订明细： " + priceInfo.toString() + "\r\n");
            sb.append("用户需求：" + request + "\r\n");
            sb.append("支付状态：已支付\r\n");
            sb.append("支付方式：授信额度\r\n");
            sb.append("------来源：MVM猫喂猫云平台------");
            crsRoomOrderReq.setRemarks(sb.toString());
            //使用crsClient， crs创建订单
            logger.info("调用mvm-drp-crs创建订单，请求参数crsRoomOrderReq===" + objectMapper.writeValueAsString(crsRoomOrderReq));
            OrderCreateRsp crsRoomOrderRsp = crsOrderService.create(crsRoomOrderReq);
            logger.info("调用mvm-drp-crs创建订单，响应参数crsRoomOrderRsp===" + objectMapper.writeValueAsString(crsRoomOrderRsp));

            OrderOpt opt = OrderOpt.ARRANGE_ROOM;  //安排房间
            Float money = Float.valueOf(order.getPayMoney().toString());
            //CRS 创建订单
            if (null != crsRoomOrderRsp && crsRoomOrderRsp.getCode().equals(ResultCode.OK.getValue())) { //成功
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

            } else {
                logger.info("CRS创建订单失败===========");
                opt = OrderOpt.APPLY_UNSUBSCRIBE;  //申请退订
                order.setStatus(4); //已拒单
                order.setUpdateTime(LocalDateTime.now());

                //退授信额度
                toRefund(order);
            }

            if (order.getId() != null) {
                //更新订单状态
                drpOrderRepository.save(order);
            }

            //订单操作类型推送给qunar
            optOrder(order, opt, money);
        } catch (Exception e) {
            logger.error("创建订单异常！otaOrderNo:" + orderInfoResponseDto.getOrderNum());
            e.printStackTrace();
        }
    }

    /**
     * 校验
     * @param everyDayPrices
     * @param dailyInfo
     * @return
     */
    private Boolean check(List<EveryDayPrice> everyDayPrices, Map<LocalDate, DailyInfoRes> dailyInfo) {
        boolean flag = true;
        for (EveryDayPrice e : everyDayPrices) {
            DailyInfoRes dailyInfoRes = dailyInfo.get(LocalDate.parse(e.getDate()));
            //房态校验
            if(!dailyInfoRes.getAvailable()){
                flag = false;
            }

            //房价校验
            if(e.getBasePrice().compareTo(dailyInfoRes.getBasePrice()) != 0){
                flag = false;
            }
        }

        return flag;
    }

    /**
     * 支付
     *
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
        SimpleRes simpleRes = qunarServiceFeignClient.pay(payReq);
        logger.info("调用授信支付，响应参数simpleRes===" + objectMapper.writeValueAsString(simpleRes));
        //支付失败
        if (simpleRes == null || simpleRes.getCode() != 200) {
            logger.error("授信支付失败！");
            order.setStatus(7); //7：失败
            order.setUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 退款
     *
     * @param order
     * @throws JsonProcessingException
     */
    private void toRefund(DrpOrder order) throws JsonProcessingException {
        PayReq payRefundReq = new PayReq();
        payRefundReq.setOrderId(Integer.decode(order.getId().toString()));
        payRefundReq.setOrderSn(order.getOrderNo());
        payRefundReq.setMoney(order.getPayMoney());
        //调用授信支付接口
        logger.info("调用退款，请求参数payRefundReq===" + objectMapper.writeValueAsString(payRefundReq));
        SimpleRes payRefundRes = qunarServiceFeignClient.payRefund(payRefundReq);
        logger.info("调用退款，响应参数payRefundRes===" + objectMapper.writeValueAsString(payRefundRes));
        //退款失败
        if (payRefundRes == null || payRefundRes.getCode() != 200) {
            logger.error("退款失败！");
        }
    }

    /**
     * 获取前一天的已接单订单，并将订单操作类型推送给qunar
     */
    public void optOrderPush() {
        List<DrpOrder> orders = drpOrderRepository.findByCheckinDate(LocalDate.now());
        orders.forEach(o -> {
            if (o.getStatus() == 2) { //已接单
                OrderDetailReq orderDetailReq = new OrderDetailReq();
                orderDetailReq.setCrsOrderId(Integer.decode(o.getCrsOrderId()));
                try {
                    //调用crs接口获取订单信息
                    logger.info("调用crs接口获取订单信息，请求参数orderDetailReq===" + objectMapper.writeValueAsString(orderDetailReq));
                    OrderDetailRsp orderDetailRsp = crsOrderService.detail(orderDetailReq);
                    logger.info("调用crs接口获取订单信息，响应参数orderDetailRsp===" + objectMapper.writeValueAsString(orderDetailRsp));

                    if (orderDetailRsp != null && orderDetailRsp.getStatusCode() != null) {
                        String statusCode = orderDetailRsp.getStatusCode();
                        if (ResStatus.NOSHOW.getStatus().equals(statusCode)) {  //未入住
                            OrderOpt opt = OrderOpt.CONFIRM_NOSHOW; //确认未入住
                            o.setCheckinStatus(0); //未入住
                            //订单操作类型推送给qunar
                            optOrder(o, opt, null);
                        } else if (ResStatus.CHECKIN.getStatus().equals(statusCode)) { //入住
                            o.setCheckinStatus(1); //已入住
                        }
                        //更新 入住状态
                        drpOrderRepository.save(o);
                    }
                } catch (Exception e) {
                    logger.error("订单操作类型推送失败，订单号：" + o.getOrderNo());
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 订单操作类型推送给qunar
     *
     * @param order 订单
     * @param opt   订单操作类型
     * @param money 用户支付金额 申请退订时才有值
     * @return
     */
    public OptOrderRequestDto optOrder(DrpOrder order, OrderOpt opt, Float money) throws JsonProcessingException {
        OptOrderRequestDto optOrderRequestDto = new OptOrderRequestDto();
        optOrderRequestDto.setOrderNum(order.getOtaOrderNo());
        optOrderRequestDto.setOpt(opt);
        if (OrderOpt.ARRANGE_ROOM.equals(opt)) {   //安排房间
            optOrderRequestDto.setArrangeType(ArrangeType.NAME);
        } else if (OrderOpt.APPLY_UNSUBSCRIBE.equals(opt)) { //申请退订
            optOrderRequestDto.setMoney(money);
        }
        //调用qunar 订单操作接口
        logger.info("调用qunar订单操作接口，请求参数optOrderRequestDto===" + objectMapper.writeValueAsString(optOrderRequestDto));
        OptOrderResponseDto optOrderResponseDto = qunarService.optOrder(optOrderRequestDto);
        logger.info("调用qunar订单操作接口，响应参数optOrderResponseDto===" + objectMapper.writeValueAsString(optOrderResponseDto));
        return optOrderRequestDto;
    }

    /**
     * 获取随机数
     * @param post 位数
     * @return
     */
    public String getRandom(int post) {
        StringBuilder sb = new StringBuilder();
        Random r = new Random(1);
        for (int i = 0; i < post; i++) {
            int ran1 = r.nextInt(10);
            sb.append(ran1);
        }
        return sb.toString();
    }

    /**
     * 查询订单列表
     * @param drpOrderReq
     * @return
     */
    public List<DrpOrderRes> queryDrpOrderList(DrpOrderReq drpOrderReq) {

        List<DrpOrderRes> drpOrderResList = new ArrayList<>();
        List<DrpOrder> drpOrderList;
        // 构造自定义查询条件
        Specification querySpeci = (Specification) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if(drpOrderReq != null){
                if (drpOrderReq.getOrderNo() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("orderNo"), "%" + drpOrderReq.getOrderNo() + "%"));
                }
                if (drpOrderReq.getOtaOrderNo() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("otaOrderNo"), "%" + drpOrderReq.getOtaOrderNo() + "%"));//模糊查询
                }
                if (drpOrderReq.getStatus() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("status"), drpOrderReq.getStatus()));
                }
                if (drpOrderReq.getHotelName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("hotelName"), "%" + drpOrderReq.getHotelName() + "%"));
                }
                if (drpOrderReq.getCreateStartTime() != null) {
                    predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), drpOrderReq.getCreateStartTime()));
                }
                if (drpOrderReq.getCreateEndTime() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("createTime"), "%" + drpOrderReq.getCreateEndTime() + "%"));
                }
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        int pageNo = 0;
        int pageSize = 0;
        if(drpOrderReq != null){
            pageNo = drpOrderReq.getPageNo();
            pageSize = drpOrderReq.getPageSize();
        }

        if(pageNo == 0 && pageSize == 0){
            drpOrderList = drpOrderRepository.findAll(querySpeci, Sort.by(Sort.Direction.DESC, "createTime"));
        } else {
            drpOrderList = drpOrderRepository.findAll(querySpeci,PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"))).getContent();
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        drpOrderList.forEach(d -> {
            DrpOrderRes drpOrderRes = new DrpOrderRes();
            drpOrderRes.setChannelCode(d.getChannelCode());
            drpOrderRes.setConfirmNo(d.getConfirmNo());
            drpOrderRes.setCheckinStatus(d.getCheckinStatus());
            drpOrderRes.setContactName(d.getContactName());
            drpOrderRes.setHotelCode(d.getHotelCode());
            drpOrderRes.setHotelType(d.getHotelType());
            drpOrderRes.setId(d.getId());
            drpOrderRes.setOrderFloorMoney(d.getOrderFloorMoney());
            drpOrderRes.setOrderNo(d.getOrderNo());
            drpOrderRes.setOtaOrderNo(d.getOtaOrderNo());
            drpOrderRes.setPayMoney(d.getPayMoney());
            drpOrderRes.setPayNo(d.getPayNo());
            drpOrderRes.setPayType(d.getPayType());
            drpOrderRes.setPhone(d.getPhone());
            drpOrderRes.setPmsConfirmNo(d.getPmsConfirmNo());
            drpOrderRes.setProductName(d.getProductName());
            drpOrderRes.setRatePlanCode(d.getRatePlanCode());
            drpOrderRes.setRemark(d.getRemark());
            drpOrderRes.setRoomCount(d.getRoomCount());
            drpOrderRes.setRoomTypeCode(d.getRoomTypeCode());
            drpOrderRes.setRoomTypeName(d.getRoomTypeName());
            drpOrderRes.setStatus(d.getStatus());
            drpOrderRes.setTotalMoney(d.getTotalMoney());
            drpOrderRes.setCashAdvanceType(d.getCashAdvanceType());
            drpOrderRes.setCheckinDate(d.getCheckinDate().toString());
            drpOrderRes.setCheckoutDate(d.getCheckoutDate().toString());
            drpOrderRes.setCreateTime(df.format(d.getCreateTime()));
            drpOrderRes.setUpdateTime(df.format(d.getUpdateTime()));
            drpOrderResList.add(drpOrderRes);
        });

        return drpOrderResList;
    }

    /**
     * 取消订单
     * @param orderId
     */
    public void cancelOrder(String orderId) throws JsonProcessingException {
        DrpOrder drpOrder = drpOrderRepository.findById(Long.decode(orderId)).get();
        logger.info("cancelOrder，更新订单前=====" + objectMapper.writeValueAsString(drpOrder));
        drpOrder.setStatus(6);
        drpOrder.setUpdateTime(LocalDateTime.now());

        //更新
        drpOrderRepository.save(drpOrder);
        logger.info("cancelOrder，更新订单后=====" + objectMapper.writeValueAsString(drpOrder));

//        PayReq payRefundReq = new PayReq();
//        payRefundReq.setOrderId(Integer.decode(drpOrder.getId().toString()));
//        payRefundReq.setOrderSn(drpOrder.getOrderNo());
//        payRefundReq.setMoney(drpOrder.getPayMoney());
//        //退款
//        logger.info("cancelOrder，退款入参=====" + objectMapper.writeValueAsString(payRefundReq));
//        SimpleRes payRefundRes = qunarServiceFeignClient.payRefund(payRefundReq);
//        logger.info("cancelOrder，退款响应=====" + objectMapper.writeValueAsString(payRefundRes));
    }
}

package com.sjlh.hotel.order.repository;

import com.sjlh.hotel.order.entity.OrderCustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderCustomerInfoRepository extends JpaRepository<OrderCustomerInfo, Long>, JpaSpecificationExecutor<OrderCustomerInfo> {

}

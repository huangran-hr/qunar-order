package com.sjlh.hotel.order.repository;

import com.sjlh.hotel.order.entity.DrpOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DrpOrderDetailRepository extends JpaRepository<DrpOrderDetail, Long>, JpaSpecificationExecutor<DrpOrderDetail> {

}

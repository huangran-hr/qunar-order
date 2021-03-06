package com.sjlh.hotel.order.repository;

import com.sjlh.hotel.order.entity.DrpOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;


public interface DrpOrderRepository extends JpaRepository<DrpOrder, Long>, JpaSpecificationExecutor<DrpOrder> {

    List<DrpOrder> findByCheckinDate(LocalDate checkInDate);

    DrpOrder findByOtaOrderNo(String otaOrderNo);
}

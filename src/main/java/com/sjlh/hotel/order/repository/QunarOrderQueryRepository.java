package com.sjlh.hotel.order.repository;

import com.sjlh.hotel.order.entity.QunarOrderQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QunarOrderQueryRepository extends JpaRepository<QunarOrderQuery, Integer>, JpaSpecificationExecutor<QunarOrderQuery> {

}

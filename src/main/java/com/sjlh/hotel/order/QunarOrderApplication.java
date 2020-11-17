/**
 *
 */
package com.sjlh.hotel.order;

import com.sjlh.hotel.crs.configurate.EnableCrsOrderClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.sjlh.qunar.client.spring.EnableQunarClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Administrator
 *
 */
@EnableFeignClients
@EnableEurekaClient
@EnableQunarClient
@EnableCrsOrderClient
@SpringBootApplication
public class QunarOrderApplication {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(QunarOrderApplication.class, args);
	}
}

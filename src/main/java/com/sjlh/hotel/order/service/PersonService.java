/**
 * 
 */
package com.sjlh.hotel.order.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sjlh.hotel.order.dto.PersonDto;

/**
 * @author Administrator
 *
 */
@FeignClient(name="PersonService", url = "http://qunar-service")
public interface PersonService {
	@RequestMapping(method = RequestMethod.POST, value="/qunar/hello", consumes = MediaType.APPLICATION_JSON_VALUE)
	PersonDto getPerson(@RequestBody PersonDto person);
	
	@RequestMapping(method = RequestMethod.POST, value="qunar/xml/hello", consumes = MediaType.APPLICATION_XML_VALUE)
	PersonDto getXmlPerson(@RequestBody PersonDto person);
}
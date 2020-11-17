///**
// *
// */
//package com.sjlh.hotel.order.controller;
//
//import javax.annotation.Resource;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.sjlh.hotel.order.dto.PersonDto;
//import com.sjlh.hotel.order.service.PersonService;
//
///**
// * @author Administrator
// *
// */
//@RestController()
//public class PersonOrderController {
//	@Resource
//	private PersonService personService;
//
//	@RequestMapping("/person/get")
//	@ResponseBody
//	public PersonDto getPerson() {
//		PersonDto p = new PersonDto();
//		p.setId(100);
//		PersonDto person = personService.getPerson(p);
//		return person;
//	}
//}

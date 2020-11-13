/**
 * 
 */
package com.sjlh.hotel.order.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Administrator
 *
 */
@Getter
@Setter
@XmlRootElement(name = "person")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonDto {
	@XmlElement(name = "id")
	private Integer id;
	@XmlElement
	private String name;
}
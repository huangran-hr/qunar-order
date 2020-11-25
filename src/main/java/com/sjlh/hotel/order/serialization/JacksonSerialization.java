/**
 *
 */
package com.sjlh.hotel.order.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author Administrator
 *
 */
public class JacksonSerialization implements Serializer<Object> {
	private ObjectMapper objectMapper;
    private String encoding = "UTF8";

    public JacksonSerialization() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule()).registerModule(new ParameterNamesModule()).registerModule(new JaxbAnnotationModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null)
            encodingValue = configs.get("serializer.encoding");
        if (encodingValue instanceof String)
            encoding = (String) encodingValue;
    }

	@Override
	public byte[] serialize(String topic, Object data) {
        try {
            if (data == null)
                return null;
            else
                return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing string to byte[] due to unsupported encoding " + encoding);
        }
	}
}

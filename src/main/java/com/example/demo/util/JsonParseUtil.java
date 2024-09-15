package com.example.demo.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonParseUtil {

	@Autowired
	protected static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * 序列化物件 為 JSON
	 */
	public static String serialize(Object target) {
		try {
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return mapper.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			ExceptionUtils.getStackTrace(e);
			log.error("Occurred JsonMapping Exception", e);
			return "";
		}
	}

	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public static <T> T unserialize(String target, Class<T> clazz) {
		try {
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}

}

package com.example.demo.base.infra.event;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.example.demo.base.infra.annotation.EventTopic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventTopicResolver {

	private final Map<Class<?>, String> eventTopicMap = new HashMap<>();

	public EventTopicResolver(Environment env) throws Exception {

		Reflections reflections = new Reflections("com.example.demo.domain");

		Set<Class<?>> eventClasses = reflections.getTypesAnnotatedWith(EventTopic.class);

		for (Class<?> clazz : eventClasses) {
			EventTopic annotation = clazz.getAnnotation(EventTopic.class);
			String topic = env.resolvePlaceholders(annotation.value());
			eventTopicMap.put(clazz, topic);
			
			log.info("建立成功: topic:{}, event:{}", topic, clazz);
		}
	}

	public String resolve(Object event) {
		String topic = eventTopicMap.get(event.getClass());

		if (topic == null) {
			throw new IllegalArgumentException("No topic mapping for event: " + event.getClass());
		}

		return topic;
	}
}
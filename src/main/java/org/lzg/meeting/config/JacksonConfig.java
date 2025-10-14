package org.lzg.meeting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

	// 统一时间格式
	private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		// 1. 注册时间模块
		JavaTimeModule module = new JavaTimeModule();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

		// 2. 添加序列化与反序列化规则
		module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
		module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

		mapper.registerModule(module);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}
}

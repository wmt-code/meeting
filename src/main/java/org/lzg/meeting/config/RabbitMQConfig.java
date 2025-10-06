package org.lzg.meeting.config;

import org.lzg.meeting.constant.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 只有当配置项message.handle.channel的值为rabbitmq时，才会加载该配置类
 */
@Configuration
@ConditionalOnProperty(name = Constants.MESSAGE_HANDLE_CHANNEL_KEY, havingValue =
		Constants.MESSAGE_HANDLE_CHANNEL_RABBITMQ)
public class RabbitMQConfig {

	public static final String EXCHANGE_NAME = "meeting.exchange";
	public static final String QUEUE_NAME = "meeting.queue";
	// public static final String ROUTING_KEY = "meeting.msg";

	// Fanout Exchange 消息群发
	@Bean
	public FanoutExchange exchange() {
		return new FanoutExchange(EXCHANGE_NAME);
	}

	// Queue
	@Bean
	public Queue queue() {
		return new Queue(QUEUE_NAME, true);
	}

	// Binding
	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queue()).to(exchange());
	}

	/**
	 * 消息转换器
	 * @return SimpleMessageConverter
	 */
	@Bean
	public SimpleMessageConverter simpleMessageConverter() {
		SimpleMessageConverter converter = new SimpleMessageConverter();
		converter.addAllowedListPatterns("org.lzg.meeting.model.dto.*");
		return converter;
	}
}

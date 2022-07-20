// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.sample;

import com.azure.spring.cloud.stream.binder.dapr.subscriber.DaprServerProperties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({ DaprServerProperties.class })
public class DaprPublisherSampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(DaprPublisherSampleApplication.class, args);
	}
	@Bean
	public BeanPostProcessor beanPostProcessor() {
		System.out.println("application 初始化了 bean BeanPostProcessor");
		return new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				System.out.println("application:加载了bean " + beanName);
				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}
		};
	}
}

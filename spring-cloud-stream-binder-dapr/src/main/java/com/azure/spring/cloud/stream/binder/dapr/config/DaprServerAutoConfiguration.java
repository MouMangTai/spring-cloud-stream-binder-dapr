// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.config;

import com.azure.spring.cloud.stream.binder.dapr.subscriber.DaprServerProperties;
import com.azure.spring.cloud.stream.binder.dapr.subscriber.DaprSpringService;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ DaprServerProperties.class })
public class DaprServerAutoConfiguration {

	@Bean
	public DaprSpringService daprSpringService(DaprServerProperties daprServerProperties) {
		return new DaprSpringService(daprServerProperties);
	}
}

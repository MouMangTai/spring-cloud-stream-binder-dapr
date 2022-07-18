// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
public class EventHubPublisherSampleApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubPublisherSampleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EventHubPublisherSampleApplication.class, args);
    }
}

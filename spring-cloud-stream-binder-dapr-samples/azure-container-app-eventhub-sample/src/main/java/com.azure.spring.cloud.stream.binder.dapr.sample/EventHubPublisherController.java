// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Event Hub Publisher controller.
 */
@RestController
@Profile("!manual")
public class EventHubPublisherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubPublisherController.class);

    @Autowired
    private StreamBridge streamBridge;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        LOGGER.info("Imperative method to send message: {} to static destination.", message);
        streamBridge.send("supply-out-0", message);
        LOGGER.info("Send {}.", message);
        return ResponseEntity.ok(message);
    }
}

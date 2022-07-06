// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.properties;

/**
 * The Dapr producer binding configuration properties.
 */
public class DaprProducerProperties {

	/**
	 * the name of the dapr pubsub component.
	 */
	private String pubsubName;

	public String getPubsubName() {
		return pubsubName;
	}

	public void setPubsubName(String pubsubName) {
		this.pubsubName = pubsubName;
	}
}

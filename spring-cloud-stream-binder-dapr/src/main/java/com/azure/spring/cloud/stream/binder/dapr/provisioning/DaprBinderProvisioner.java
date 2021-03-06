// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr.provisioning;

import com.azure.spring.cloud.stream.binder.dapr.properties.DaprConsumerProperties;
import com.azure.spring.cloud.stream.binder.dapr.properties.DaprProducerProperties;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * The {@link DaprBinderProvisioner} is responsible for the provisioning of consumer and producer destinations.
 */
public class DaprBinderProvisioner
		implements
		ProvisioningProvider<ExtendedConsumerProperties<DaprConsumerProperties>,
				ExtendedProducerProperties<DaprProducerProperties>> {

	@Override
	public ProducerDestination provisionProducerDestination(String name,
			ExtendedProducerProperties<DaprProducerProperties> properties) throws ProvisioningException {
		return new DaprProducerDestination(name);
	}

	@Override
	public ConsumerDestination provisionConsumerDestination(String name, String group,
			ExtendedConsumerProperties<DaprConsumerProperties> properties) throws ProvisioningException {
		return new DaprConsumerDestination(name);
	}

	private static final class DaprProducerDestination implements ProducerDestination {

		private final String topic;

		DaprProducerDestination(String topic) {
			this.topic = topic.trim();
		}

		@Override
		public String getName() {
			return topic;
		}

		@Override
		public String getNameForPartition(int partition) {
			return topic + "-" + partition;
		}
	}

	private static final class DaprConsumerDestination implements ConsumerDestination {

		private final String topic;

		DaprConsumerDestination(final String topic) {
			this.topic = topic.trim();
		}

		@Override
		public String getName() {
			return topic;
		}
	}
}

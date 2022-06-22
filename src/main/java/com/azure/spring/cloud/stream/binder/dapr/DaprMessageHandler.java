// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.dapr;

import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.dapr.v1.DaprGrpc;
import io.dapr.v1.DaprProtos;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

/**
 * The {@link DaprMessageHandler} wraps a {@link DaprGrpc.DaprStub} to publish events.
 */
public class DaprMessageHandler implements MessageHandler {

	private DaprGrpc.DaprStub daprStub;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	/**
	 * Construct a {@link DaprMessageHandler} with {@link DaprGrpc.DaprStub}.
	 *
	 * @param daprStub the dapr grpc stub
	 */
	DaprMessageHandler(DaprGrpc.DaprStub daprStub) {
		this.daprStub = daprStub;
	}

	@Override
	public void handleMessage(Message<?> message) {
		String TOPIC_NAME = "que";
		String PUBSUB_NAME = "servicebus-pubsub";
		this.publishEvent(PUBSUB_NAME, TOPIC_NAME, message.getPayload()).block();
	}

	/**
	 * Publish event to specified topic of specified pubsub.
	 *
	 * @param pubsub the pubsub component name
	 * @param topic	the topic
	 * @param data the data
	 * @return Mono
	 */
	private Mono<Void> publishEvent(String pubsub, String topic, Object data) {
		try {
			DaprProtos.PublishEventRequest.Builder envelopeBuilder = DaprProtos.PublishEventRequest.newBuilder()
					.setTopic(topic)
					.setPubsubName(pubsub)
					.setData(ByteString.copyFrom(OBJECT_MAPPER.writeValueAsBytes(data)));

			return Mono.subscriberContext().flatMap(
					context ->
							this.<Empty>createMono(
									it -> daprStub.publishEvent(envelopeBuilder.build(), it)
							)
			).then();
		}
		catch (Exception ex) {
			return Mono.error(ex);
		}
	}

	private <T> Mono<T> createMono(Consumer<StreamObserver<T>> consumer) {
		return Mono.create(sink -> {
			consumer.accept(createStreamObserver(sink));
		});
	}

	private <T> StreamObserver<T> createStreamObserver(MonoSink<T> sink) {
		return new StreamObserver<T>() {
			@Override
			public void onNext(T value) {
				sink.success(value);
			}

			@Override
			public void onError(Throwable t) {
				sink.error(new Throwable());
			}

			@Override
			public void onCompleted() {
				sink.success();
			}
		};
	}
}
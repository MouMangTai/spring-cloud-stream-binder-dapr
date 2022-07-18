# LOCAL PUBLISHER SAMPLE

This is a local publisher sample of Spring Cloud Stream Binder Dapr, demonstrate how to use dapr binder to send messages. 

## Pre-requisites
- [Dapr and Dapr Cli](https://docs.dapr.io/getting-started/install-dapr-cli/)
- [Azure Event Hub](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create)
## QuickStart

### 1. Configure the Pub/Sub component

Replace the `<CONNECTION_STRING>` placeholder with Event Hub connection string in the `pubsub.yaml` file. This file helps enable your Dapr app to access your Event Hub.
Refer [Get Event Hub Connection String](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string#azure-cli) for namespace connection string
```yaml
apiVersion: dapr.io/v1alpha1
kind: Component
metadata:
  name: eventhubs-pubsub
  namespace: default
spec:
  type: pubsub.azure.eventhubs
  version: v1
  metadata:
    - name: connectionString
      value: "<CONNECTION_STRING>"
```

Replace the `<EVENT_HUB_NAME>` placeholder with Event Hub name in the `application.yaml` file.
```yaml
spring:
  cloud:
    stream:
      function:
        definition: supply
      bindings:
        supply-out-0:
          destination: <EVENT_HUB_NAME>
      dapr:
        bindings:
          supply-out-0:
            producer:
              pubsubName: eventhubs-pubsub
```

### 2. Run Application with Dapr sidecar
```shell
cd spring-cloud-stream-binder-dapr-sample
dapr run --app-id publisher --app-port 9090 --components-path ./components  --app-protocol grpc --dapr-grpc-port 50001 mvn spring-boot:run
```

### 3. Output

```shell
Sending message, sequence 0
Success to publish event
Sending message, sequence 1
Success to publish event
Sending message, sequence 2
Success to publish event
Sending message, sequence 3
Success to publish event
Sending message, sequence 4
Success to publish event
...


------onTopicEvent------
TopicEventRequest :
id: "34e3efdb-20bc-4551-83bf-432049e6b1f9"
source: "publisher"
type: "com.dapr.event.sent"
spec_version: "1.0"
data_content_type: "application/json"
topic: "topic"
data: data: "{\"payload\":\"SGVsbG8gd29ybGQsIDM=\",\"headers\":{\"contentType\":\"application/json\",\"id\":\"7feb48ab-f55d-4f74-7292-23a458080cf9\",\"timestamp\":1657504528684}}"
pubsub_name: "eventhubs-pubsub"
...
```

### 4. Stop Dapr Sidecar
```shell
dapr stop --app-id publisher
```

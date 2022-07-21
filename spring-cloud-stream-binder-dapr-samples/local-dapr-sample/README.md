# LOCAL DAPR SAMPLE

This is a local publisher sample of Spring Cloud Stream Binder Dapr, demonstrate how to use Dapr binder to send and receive messages. 

## Pre-requisites
- [Dapr and Dapr Cli](https://docs.dapr.io/getting-started/install-dapr-cli/)
- [Azure Event Hubs](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create)
- [Azure Storage Account](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-create?tabs=azure-portal)
- [JDK8](https://www.oracle.com/java/technologies/downloads/) or later
- Maven
- You can also import the code straight into your IDE:
    - [IntelliJ IDEA](https://www.jetbrains.com/idea/download)

## QuickStart

### 1. Configure the Pub/Sub component

Replace the `<AZURE_CONNECTION_STRING>` placeholder with Event Hub connection string in the `pubsub.yaml` file. This file helps enable your Dapr app to access your Event Hub.
Refer [Get Event Hub Connection String](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string#azure-cli) for namespace connection string.

Replace the `<AZURE_STORAGE_ACCOUNT_NAME>`、`<AZURE_STORAGE_ACCOUNT_KEY>` and `<AZURE_STORAGE_CONTAINER_NAME>` placeholders.
Refer [Manage storage account access keys](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage?tabs=azure-portal) for information such as storage account access keys.
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
      value: "<AZURE_CONNECTION_STRING>"
    - name: storageAccountName
      value: "<AZURE_STORAGE_ACCOUNT_NAME>"
    - name: storageAccountKey
      value: "<AZURE_STORAGE_ACCOUNT_KEY>"
    - name: storageContainerName
      value: "<AZURE_STORAGE_CONTAINER_NAME>"
```

Replace the `<AZURE_EVENTHUB_NAME>` placeholder with Event Hub name in the `application.yaml` file.
```yaml
spring:
  cloud:
    stream:
      function:
        definition: supply;consume
      bindings:
        supply-out-0:
          destination: <AZURE_EVENTHUB_NAME>
        consume-in-0:
          destination: <AZURE_EVENTHUB_NAME>
      dapr:
        bindings:
          supply-out-0:
            producer:
              pubsubName: eventhubs-pubsub
          consume-in-0:
            consumer:
              pubsubName: eventhubs-pubsub
```
```

### 2. Run Application with Dapr sidecar
```shell
cd local-dapr-sample
dapr run --app-id publisher --app-port 9090 --components-path ./components  --app-protocol grpc --dapr-grpc-port 50001 mvn spring-boot:run
```

### 3. Output

```shell
Sending message, sequence 0
Success to publish event
Message received, <ByteString@1362d7ec size=153 contents="{\"payload\":\"SGVsbG8gd29ybGQsIHt9MzA=\",\"headers\"...">, headers, {contentType=application/json, id=88e12cd8-872a-27cf-0f84-aa8577578622, timestamp=1658330390826}
Sending message, sequence 1
Success to publish event
Message received, <ByteString@26ebb10a size=153 contents="{\"payload\":\"SGVsbG8gd29ybGQsIHt9Mjk=\",\"headers\"...">, headers, {contentType=application/json, id=7efd3867-f04a-725b-1f78-2a80fab0037a, timestamp=1658330389814}
...
```

### 4. Stop Dapr Sidecar
```shell
dapr stop --app-id publisher
```

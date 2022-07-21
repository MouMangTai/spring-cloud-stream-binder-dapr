# AZURE CONTAINER APP DAPR SAMPLE

In this example, you deploy a web service app to [Azure Container Apps](https://azure.microsoft.com/services/container-apps/).
And the app demonstrates how to use `Spring Cloud Stream Dapr Binder` to send and receive messages.

## Prerequisites
- [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli)
- [Azure Event Hubs](https://docs.microsoft.com/azure/event-hubs/event-hubs-quickstart-cli)
- [Azure Subscription](https://azure.microsoft.com/free/)
- [Azure Storage Account](https://docs.microsoft.com/azure/storage/common/storage-account-create?tabs=azure-portal)
- [Docker](https://www.docker.com/get-started/)

## 1. Create a Container Apps environment for your container apps

### 1.1. Set up
First, sign in to Azure from the CLI. Run the following command, and follow the prompts to complete the authentication process.
```shell
az login
```
Set the current subscription context. Replace `MyAzureSub` with the name of the Azure subscription you want to use:
```shell
az account set --subscription <MyAzureSub>
```
Next, install the Azure Container Apps extension for the CLI.
```shell
az extension add --name containerapp --upgrade
```
Now that the extension is installed, register the `Microsoft.App` namespace.
```shell
az provider register --namespace Microsoft.App
```

> ***NOTE:***
> Azure Container Apps resources have migrated from the `Microsoft.Web` namespace to the `Microsoft.App` namespace. Refer to [Namespace migration from Microsoft.Web to Microsoft.App in March 2022](https://github.com/enterprises/microsoftopensource/saml/initiate?return_to=https%3A%2F%2Fgithub.com%2Fmicrosoft%2Fazure-container-apps%2Fissues%2F109) for more details.

Next, set the following environment variables:
```shell
RESOURCE_GROUP="dapr-container-apps"
LOCATION="canadacentral"
CONTAINERAPPS_ENVIRONMENT="dapr-container-environment"
```
With these variables defined, you can create a resource group to organize the services related to your new container app.

### 1.2. Create a resource group
```shell
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

With the CLI upgraded and a new resource group available, you can create a Container Apps environment.

### 1.3. Create a container apps environment

An environment in Azure Container Apps creates a secure boundary around a group of container apps. Container Apps deployed to the same environment are deployed in the same virtual network and write logs to the same Log Analytics workspace.

Individual container apps are deployed to an Azure Container Apps environment. To create the environment, run the following command:

```shell
az containerapp env create \
  --name $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --location "$LOCATION"
```


## 2. Configure the Pub/Sub component to connect to Azure Event Hubs

Replace the `<CONNECTION_STRING>` placeholder with Event Hub connection string in the `pubsub.yaml` file. This file helps enable your Dapr app to access your Event Hub.
Refer [Get Event Hub Connection String](https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string#azure-cli) for namespace connection string.

Replace the `<STORAGE_ACCOUNT_NAME>`、`<STORAGE_ACCOUNT_KEY>` and `<STORAGE_CONTAINER_NAME>` placeholders.
Refer [Manage storage account access keys](https://docs.microsoft.com/azure/storage/common/storage-account-keys-manage?tabs=azure-portal) for information such as storage account access keys.
```yaml
componentType: pubsub.azure.eventhubs
version: v1
metadata:
  - name: connectionString
    value: "<CONNECTION_STRING>"
  - name: storageAccountName
    value: "<STORAGE_ACCOUNT_NAME>"
  - name: storageAccountKey
    value: "STORAGE_ACCOUNT_KEY"
  - name: storageContainerName
    value: "STORAGE_CONTAINER_NAME"
```

Replace the `<EVENT_HUB_NAME>` placeholder with Event Hub name in the `application.yaml` file.
```yaml
spring:
  cloud:
    stream:
      function:
        definition: supply;consume
      bindings:
        supply-out-0:
          destination: <EVENT_HUB_NAME>
        consume-in-0:
          destination: <EVENT_HUB_NAME>
      dapr:
        bindings:
          supply-out-0:
            producer:
              pubsubName: eventhubs-pubsub
          consume-in-0:
            consumer:
              pubsubName: eventhubs-pubsub
```

Run the following command to configure the Dapr component in the Container Apps environment.

If you need to add multiple components, create a separate YAML file for each component and run the az containerapp env dapr-component set command multiple times to add each component. For more information about configuring Dapr components, see [Configure Dapr components](https://docs.microsoft.com/azure/container-apps/dapr-overview#configure-dapr-components).

```shell
az containerapp env dapr-component set \
    --name $CONTAINERAPPS_ENVIRONMENT \
    --resource-group $RESOURCE_GROUP \
    --dapr-component-name eventhubs-pubsub \
    --yaml ./cloud-components/pubsub.yaml
```

## 3. Build and push a container image to Azure Container Registry

Set the Azure Container Registry variable:
```shell
REGISTRY_NAME="daprsampleregistry"
```

With these variables defined, you can create a private Azure Container Registry in a resource group.
```shell
az acr create \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --name $REGISTRY_NAME \
    --sku Standard
```

Updates the `<properties>` collection in the `pom.xml` file. Replace the placeholder with the registry name.
```xml
<properties>
    <docker.image.prefix>$REGISTRY_NAME.azurecr.io</docker.image.prefix>
</properties>
```

Run the following commands to build and push the image to the registry, The `az config` command sets the default registry name to be used in the az acr command.
```shell
az config set defaults.acr=$REGISTRY_NAME
az acr login && mvn compile jib:build
```

Update a registry using the `az acr update` command and pass the --anonymous-pull-enabled parameter. By default, anonymous pull is disabled in the registry.
```shell
az acr update --name $REGISTRY_NAME --anonymous-pull-enabled --admin-enabled true
```

> ***NOTE:***
> By default, access to pull or push content from an Azure container registry is only available to [authenticated](https://docs.microsoft.com/azure/container-registry/container-registry-authentication?tabs=azure-cli) users. Enabling anonymous (unauthenticated) pull access makes all registry content publicly available for read (pull) actions. Anonymous pull access can be used in scenarios that do not require user authentication such as distributing public container images.

## 4. Deploy the application (HTTP web server)

```shell
az containerapp create \
  --name dapr-app \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image $REGISTRY_NAME.azurecr.io/azure-container-app-dapr-sample \
  --enable-dapr \
  --dapr-app-id dapr-app \
  --dapr-app-port 50001 \
  --dapr-app-protocol grpc \
  --target-port 8080 \
  --ingress 'external'
```

This command deploys:
- the service app server on --target-port 8080 (the app port)
- its accompanying Dapr sidecar configured with `--dapr-app-id dapr-app` and `--dapr-app-port 50001` for service discovery and invocation.

## 5. Verify the result
Once the deployment succeeds, it will display the container's fully qualified domain name (FQDN).

For example:
```shell
"fqdn": "dapr-app.calmwater-80336c7e.canadacentral.azurecontainerapps.io",
```

Run the following commands to send the specified message:

```shell
curl -X POST https://dapr-app.calmwater-80336c7e.canadacentral.azurecontainerapps.io/send?message=hello
```

Register the `Microsoft.OperationalInsights` provider for the Azure Monitor Log Analytics Workspace if you have not used it before.
```shell
az provider register --namespace Microsoft.OperationalInsights
```

Data logged via a container app are stored in the ContainerAppConsoleLogs_CL custom table in the Log Analytics workspace. You can view logs through the Azure portal or with the CLI. Wait a few minutes for the analytics to arrive for the first time before you're able to query the logged data.

Use the following CLI command to view logs on the command line.

```shell
LOG_ANALYTICS_WORKSPACE_CLIENT_ID=`az containerapp env show --name $CONTAINERAPPS_ENVIRONMENT --resource-group $RESOURCE_GROUP --query properties.appLogsConfiguration.logAnalyticsConfiguration.customerId --out tsv`

az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == 'dapr-app' | project ContainerAppName_s, Log_s, TimeGenerated"\
  --out table
```

## 6.Clean up resources
Once you're done, run the following command to delete your resource group along with all the resources you created in this tutorial.

```shell
az group delete \
    --resource-group $RESOURCE_GROUP
```

This command deletes the resource group that includes all of the resources created in this tutorial.

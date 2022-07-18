# AZURE CONTAINER APP PUBLISHER SAMPLE

In this example, you deploy a web service app to `Azure Container Apps`.
And the app demonstrates how to use `event hub binder` to send messages.

## Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- [Azure Event Hub](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quickstart-cli)
- [Azure subscription](https://azure.microsoft.com/free/)
- [JDK8](https://www.oracle.com/java/technologies/downloads/) or later
- Maven
- You can also import the code straight into your IDE:
    - [IntelliJ IDEA](https://www.jetbrains.com/idea/download)

## 1. Create a Container Apps environment for your container apps

### 1.1. Set up
First, sign in to Azure from the CLI. Run the following command, and follow the prompts to complete the authentication process.
```shell
az login
```
Set the current subscription context. Replace MyAzureSub with the name of the Azure subscription you want to use:
```shell
az account set --subscription <MyAzureSub>
```
Next, install the Azure Container Apps extension for the CLI.
```shell
az extension add --name containerapp --upgrade
```
Now that the extension is installed, register the Microsoft.App namespace.
```shell
az provider register --namespace Microsoft.App
```
Register the `Microsoft.OperationalInsights` provider for the Azure Monitor Log Analytics Workspace if you have not used it before.
```shell
az provider register --namespace Microsoft.OperationalInsights
```
Next, set the following environment variables:
```shell
RESOURCE_GROUP="eventhub-container-apps"
LOCATION="canadacentral"
CONTAINERAPPS_ENVIRONMENT="eventhub-container-environment"
```
With these variables defined, you can create a resource group to organize the services related to your new container app.

### 1.2. Create a resource group
```shell
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

With the CLI upgraded and a new resource group available, you can create a Container Apps environment.

### 1.3. Create an environment

An environment in Azure Container Apps creates a secure boundary around a group of container apps. Container Apps deployed to the same environment are deployed in the same virtual network and write logs to the same Log Analytics workspace.

Individual container apps are deployed to an Azure Container Apps environment. To create the environment, run the following command:

```shell
az containerapp env create \
  --name $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --location "$LOCATION"
```


## 2. Configure EventHub property
Replace the `<CONNECTION_STRING>` placeholder with Event Hub connection string and `<EVENT_HUB_NAME>` placeholder with Event Hub name in the `application.yaml` file.
This file helps enable your app to access your Event Hub.
Refer [Get Event Hub Connection String](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string#azure-cli) for namespace connection string
```yaml
spring:
  cloud:
    azure:
      eventhubs:
        connection-string: <CONNECTION_STRING>
    stream:
      function:
        definition: supply
      bindings:
        supply-out-0:
          destination: <EVENT_HUB_NAME>
      eventhubs:
        bindings:
          supply-out-0:
            producer:
              sync: true
```

## 3. Build and push a container image to Azure Container Registry

Set the Azure Container Registry variable:
```shell
REGISTRY_NAME="eventhubsampleregistry"
```

With these variables defined, you can create create a private Azure Container Registry in a resource group.
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
> By default, access to pull or push content from an Azure container registry is only available to [authenticated](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-authentication?tabs=azure-cli) users. Enabling anonymous (unauthenticated) pull access makes all registry content publicly available for read (pull) actions. Anonymous pull access can be used in scenarios that do not require user authentication such as distributing public container images.

## 4. Deploy the application (HTTP web server)

```shell
az containerapp create \
  --name eventhub-publisher-app \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image $REGISTRY_NAME.azurecr.io/azure-container-app-publisher-eventhub \
  --target-port 8080 \
  --ingress 'external'
```

This command deploys:
- the service app server on --target-port 8080 (the app port)

## 5. Verify the result
Once the deployment succeeds, it will display the container's fully qualified domain name (FQDN).

For example:
```shell
"fqdn": "eventhub-publisher-app.calmwater-80336c7e.canadacentral.azurecontainerapps.io",
```

Run the following commands to send the specified message:

```shell
curl -X POST https://eventhub-publisher-app.calmwater-80336c7e.canadacentral.azurecontainerapps.io?message=hello
```

Data logged via a container app are stored in the ContainerAppConsoleLogs_CL custom table in the Log Analytics workspace. You can view logs through the Azure portal or with the CLI. Wait a few minutes for the analytics to arrive for the first time before you're able to query the logged data.

Use the following CLI command to view logs on the command line.

```shell
LOG_ANALYTICS_WORKSPACE_CLIENT_ID=`az containerapp env show --name $CONTAINERAPPS_ENVIRONMENT --resource-group $RESOURCE_GROUP --query properties.appLogsConfiguration.logAnalyticsConfiguration.customerId --out tsv`

az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == 'eventhub-publisher-app' | project ContainerAppName_s, Log_s, TimeGenerated"\
  --out table
```

## 6.Clean up resources
Once you're done, run the following command to delete your resource group along with all the resources you created in this tutorial.

```shell
az group delete \
    --resource-group $RESOURCE_GROUP
```

This command deletes the resource group that includes all of the resources created in this tutorial.

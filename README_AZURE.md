# Azure Container Apps deployment

This guide describes the deployment flow; use your own Azure resource names and keep credentials in Azure secrets or a secret manager, never in this repository.

## Prerequisites

- Azure CLI, Docker, and access to an Azure subscription
- An Azure Container Registry (ACR), Container Apps environment, and MariaDB-compatible database
- An Azure app registration for OIDC sign-in

## Build and publish the image

Build the application and container image from the repository root:

```sh
mvn -DskipTests package
docker build -t vokabel-server:latest .
```

Sign in to your registry, tag the image with its registry host, and push it:

```sh
az acr login --name <ACR_NAME>
docker tag vokabel-server:latest <ACR_LOGIN_SERVER>/vokabel-server:latest
docker push <ACR_LOGIN_SERVER>/vokabel-server:latest
```

Configure the Container App to pull from ACR using a managed identity with the `AcrPull` role. Avoid enabling registry admin credentials for routine deployment.

## Configure the Container App

Create the app in an existing Container Apps environment with external HTTPS ingress and target port `8080`, or update an existing app to the new image:

```sh
az containerapp update \
  --name <APP_NAME> \
  --resource-group <RESOURCE_GROUP> \
  --image <ACR_LOGIN_SERVER>/vokabel-server:latest
```

For a first deployment, use `az containerapp create` with your environment, registry identity, ingress, and image settings. Configure these application environment variables on the app:

- `CLIENT_ID`, `CLIENT_SECRET`, `TENANT_ID`
- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`

Store sensitive values as Container Apps secrets and expose them to the container through secret references. Do not print secrets in build output or commit them to files. The application also uses forwarded headers when running behind the Azure ingress.

## Configure Azure sign-in

Register this redirect URI in the Azure app registration, replacing the host with the Container App's HTTPS domain:

```text
https://<APP_FQDN>/login/oauth2/code/azure
```

Assign the appropriate application role to each user. The application recognizes `POWERUSER`, `PowerUser`, `VokabelPowerUser`, `VISITOR`, `Visitor`, and `VokabelVisitor` values in the OIDC `roles` or `groups` claims. Group object IDs are not mapped automatically. See [Authentication and roles](docs/AUTHENTICATION_AND_ROLES.md).

## Verify and update

Use the Azure-generated application FQDN to open the site and verify sign-in and database connectivity. The health endpoint used by the container health check is `/actuator/health`.

For an update, build and push a new image, then run the `az containerapp update` command above with the new image tag. Container Apps creates a new revision. Use immutable version tags for releases rather than relying on `latest`.

Azure Container Apps deployment (overview)

1) Build the fat JAR locally:
   mvn -DskipTests package

2) Build and push image to Azure Container Registry (ACR):
   # replace <RG>, <REGION>, <ACR_NAME>
   az group create -n <RG> -l <REGION>
   az acr create -n <ACR_NAME> -g <RG> --sku Standard --admin-enabled true
   az acr login -n <ACR_NAME>
   docker build -t vokabel-server:latest .
   docker tag vokabel-server:latest <ACR_NAME>.azurecr.io/vokabel-server:latest
   docker push <ACR_NAME>.azurecr.io/vokabel-server:latest

3) Create Container Apps environment and Container App (public ingress):
   # replace <RG>, <REGION>, <ENV>, <ACR_NAME>
   az containerapp env create -g <RG> -n <ENV> -l <REGION>
   az containerapp create \
     --name vokabel-app --resource-group <RG> --environment <ENV> \
     --image <ACR_NAME>.azurecr.io/vokabel-server:latest \
     --ingress external --target-port 8080 \
     --registry-server <ACR_NAME>.azurecr.io \
     --registry-username $(az acr credential show -n <ACR_NAME> --query username -o tsv) \
     --registry-password $(az acr credential show -n <ACR_NAME> --query passwords[0].value -o tsv)

4) Verify: az containerapp show -g <RG> -n vokabel-app --query properties.configuration.ingress.fqdn -o tsv

Notes: adjust JAVA_OPTS via --env-vars when creating the containerapp if you need memory limits or JVM tuning. For a fully automated CI/CD, build the image in GitHub Actions and push to ACR, then use 'az containerapp revision set-mode' and roll updates.
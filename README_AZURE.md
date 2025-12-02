Azure Container Apps deployment (overview)

1) Build the fat JAR locally:
   mvn -DskipTests package

2) Build and push image to Azure Container Registry (ACR):
   az group create -n rg_app-test -l germanywestcentral
   # az provider register --namespace Microsoft.ContainerRegistry --wait
   az acr create -n molfiloacr-g rg_app-test --sku Standard --admin-enabled true
   az acr login -n molfiloacr
   docker build -t vokabel-server:latest .
   docker tag vokabel-server:latest molfiloacr.azurecr.io/vokabel-server:latest
   docker push molfiloacr.azurecr.io/vokabel-server:latest

3) Create Container Apps environment and Container App (public ingress):
   # az provider register -n Microsoft.OperationalInsights --wait
   az containerapp env create -g rg_app-test -n env-app-test -l germanywestcentral
   az containerapp create --name vokabel-app --resource-group rg_app-test --environment env-app-test --image molfiloacr.azurecr.io/vokabel-server:latest --ingress external --target-port 8080 --registry-server molfiloacr.azurecr.io --registry-username $(az acr credential show -n molfiloacr --query username -o tsv) --registry-password $(az acr credential show -n molfiloacr --query passwords[0].value -o tsv)

4) Verify: az containerapp show -g rg_app-test -n vokabel-app --query properties.configuration.ingress.fqdn -o tsv

5)
how can i update the application?

   Manual update: edit code, build the fat JAR and image, then push the image to your registry — e.g. mvn -DskipTests
   package && docker build -t vokabel-server:latest . && docker tag vokabel-server:latest <ACR>
   .azurecr.io/vokabel-server:latest && az acr login -n <ACR> && docker push <ACR>.azurecr.io/vokabel-server:latest.
   Deploy the new image to Azure Container Apps (creates a new revision): az containerapp update --name vokabel-app -g
   <RG> --image <ACR>.azurecr.io/vokabel-server:latest (or use az containerapp create if first deploy); the CA service
   will roll the new revision live.
   For PWA clients, bump your SW cache name or change FILES_TO_CACHE and call self.skipWaiting()/clients.claim() in
   sw.js so browsers fetch the new assets (and optionally show a “Reload to update” prompt to users).
   
   
Notes: adjust JAVA_OPTS via --env-vars when creating the containerapp if you need memory limits or JVM tuning. For a fully automated CI/CD, build the image in GitHub Actions and push to ACR, then use 'az containerapp revision set-mode' and roll updates.
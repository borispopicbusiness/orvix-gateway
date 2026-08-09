# Orvix Gateway

**Orvix Gateway** is a Spring Boot based microservice using Spring Cloud and WebFlux which relies on **Project Reactor**.

⚠️⚠️⚠️ Caution: This is just the beginning. The gateway is not yet functional; development is ongoing.

## Tech stack

    - Java 21 / Spring Boot 3.3.4 / Spring Cloud / Spring Kubernetes / Spring Security
    - Maven 3.9.5
    - Docker / Kubernetes / Calico / Traefik / MetalLB / Sealed Secrets

## Build and Run

   1. **Build the project, downloading the dependencies, and run the tests**

    mvn clean install

    mvn test

   2. **Package the project into a fat/executable JAR**

    mvn clean package

   3. **Run the application via Maven**

    mvn spring-boot:run    

   4. **Run directly the packaged JAR**

    java -jar target/orvix-gateway-0.0.1-SNAPSHOT.jar

## Build Docker image

The Dockerfile is located in the root directory of the project. To build a docker image, run:

```bash
docker build -t gateway:latest .
```

This command builds a new gateway image and tags it as `latest`

Before pushing the image to the Docker repository dedicated to the gateway service, I also tag it with my Docker Hub namespace:

```bash
docker tag gateway:latest borispopicdev/orvix-gateway:latest
```

Finally, push the newly built image:

```bash
docker push borispopicdev/orvix-gateway:latest
```

⚠️⚠️⚠️ Make sure you are in the root directory of the project when executing these commands.

## About docker-compose.yaml

The docker-compose.yaml file is located in the root of the repository. For now, it contains only the Keycloak container definition,
along with the `orvix-local-dev-network` network configuration and three volumes for the keycloak container. In local development
setup Keycloak runs with an H2 in-memory database. I decided not to introduce a PostgresSQL container for at this early stage of
the gateway's development, but this will be changed relatively soon.

Now, regarding the main topic (see the task [OX-27](https://bpbu.atlassian.net/browse/OX-27)
and [the merge/pull request](https://github.com/borispopicbusiness/orvix-gateway/pull/7)), I think it is important to highlight
the role of `mem_limit` and how it relates to Docker v2/3, Docker Swarm etc.

The mem_limit field is still recognized for backward compatibility in Compose file format v2, but it is deprecated in v3,
which was designed primarily for Docker Swarm deployments. In Swarm mode, resource constraints must be defined under the `deploy`
section instead (`deploy.resources.limits.memory`). Since Compose v3 ignores mem_limit outside of Swarm, it can cause confusion
depending on the environment and tooling being used.

| Compose version | Field                          | IntelliJ warning | Works in Docker? |
|-----------------|--------------------------------|------------------|------------------|
| v2              | mem_limit                      | &#x274C; No      | &#x2714; Yes     |
| v3+(non-Swarm)  | mem_limit                      | &#x26A0; Yes     | &#x2714; Yes     |
| v3+(Swarm)      | deploy.resources.limits.memory | &#x274C; No      | &#x2705; Yes     |

The compose version used here is v3.9, and I do not use Docker Swarm for local development. This leaves us with two options,
downgrade the compose version to v2, which officially incorporates `mem_limit`, or stay with v3.9, which is backward compatible with v2,
and accept the error raised by Intellij.

Given that v3.9 is backward compatible and Swarm is not involved, keeping v3.9 is a reasonable choice for local development, even if it triggers an IDE warning.

Regarding the docker swarm approach

```yaml
services:
  keycloak:
    deploy:
      resources:
        limits:
          memory: 2G
```

Since Docker Swarm is not used here, this section this part of the configuration is ignored whenever the container is started locally.
It should be noted that `mem_limit` is still supported by Docker. Compose version 3.x incorporates it for backward compatibility with v2,
which officially supports `mem_limit`.

That said, we can inspect the effect of the property by running

```bash
docker inspect keycloak-local-dev
```

and we see `"Memory"` in 

```json
"HostConfig": {
            "Binds": null,
            "ContainerIDFile": "",
            "LogConfig": {
                "Type": "json-file",
                "Config": {}
            },
            "NetworkMode": "orvix-gateway_network",
            ...
            "Memory": 2147483648,
	    ...
```

or by executing

```bash
docker inspect keycloak-local-dev | jq ".[0].HostConfig.Memory"
```

Remember

```bash
docker inspect <image-name>
```

does not show the `Memory` field.

## Microservice Endpoints

The microservice exposes the following Actuator endpoints, which can be used for health checks and informational purposes in a Kubernetes environment:

| Endpoint                                     | Description                                                |
|----------------------------------------------|------------------------------------------------------------|
| **`/actuator/health`**                       | Returns the health status of the application               |
| **`/actuator/info`**                         | Provides general application information                   |
| **`/api/v1/diagnostics/cloud/services/all`** | Generates the list of available services in the k8s kuster | 

### Testing Locally

You can test the endpoints locally using `curl`:

```bash
curl http://localhost:8080/actuator/health
```

and

```bash
curl http://localhost:8080/actuator/info
```

### Testing in the dev environment in the k8s cluster

This is a temporary output from the curl command that is shown below, it is executed against the gateway pod in the k8s cluster:

```bash
curl -H "Host: gateway.dev.k8s-svc.homelab" http://192.168.1.241:32582/actuator/info
```

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$ curl -H "Host: gateway.dev.k8s-svc.homelab" http://192.168.1.241:32582/actuator/info | jq
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
    Dload  Upload   Total   Spent    Left  Speed
    100   276  100   276    0     0  10612      0 --:--:-- --:--:-- --:--:-- 11040
    {
        "app": {
            "name": "Orvix Gateway",
            "version": "1.0.0",
            "description": "Orvix Gateway microservice"
        },
        "kubernetes": {
            "nodeName": "k8s-wn1",
            "podIp": "192.168.208.106",
            "hostIp": "192.168.1.215",
            "namespace": "dev",
            "podName": "gateway-868759df7d-wp6sz",
            "serviceAccount": "gateway",
            "inside": true
        }
    }
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$ 

I used jq to format the JSON output for readability.

To verify that the pod is running I usually use the following command:

```bash
kubectl get pods -namespace dev
```

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$ kubectl get pods -n dev
    NAME                       READY   STATUS    RESTARTS      AGE
    gateway-868759df7d-wp6sz   1/1     Running   3 (59m ago)   61m
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$

The dev namespace is used for development-related deployments. Additional namespaces (such as stage and prod) will be introduced for staging and production environments.

## Deployment of Orvix Gateway

Please check the [orvix-infra repository](https://github.com/borispopicbusiness/orvix-infra)

To deploy (or upgrade) the Orvix Gateway microservice in the dev environment, I use the following Helm command:

```bash
helm upgrade --install gateway ./helm/gateway/ -f ./helm/gateway/values-dev.yaml --namespace dev
```

This command installs [the Helm chart](https://github.com/borispopicbusiness/orvix-infra/tree/develop/helm/gateway) if it’s not already present, or upgrades it if it is.
The [values-dev.yaml](https://github.com/borispopicbusiness/orvix-infra/blob/develop/helm/gateway/values-dev.yaml) file contains environment-specific configuration for the development environment.

After the deployment completes, I verify that the gateway deployment is running by checking the deployments in the dev namespace:

```bash
kubectl get deployments -n dev
```

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$ kubectl get deployments -n dev
    NAME      READY   UP-TO-DATE   AVAILABLE   AGE
    gateway   1/1     1            1           57m
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$

To inspect all Kubernetes resources related to the Orvix Gateway deployment in the dev environment, I use:

```bash
kubectl get all -n dev 
```

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$ kubectl get all -n dev
    NAME                           READY   STATUS    RESTARTS      AGE
    pod/gateway-868759df7d-wp6sz   1/1     Running   3 (61m ago)   63m
    
    NAME              TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
    service/gateway   ClusterIP   10.99.115.79   <none>        8080/TCP   63m
    
    NAME                      READY   UP-TO-DATE   AVAILABLE   AGE
    deployment.apps/gateway   1/1     1            1           63m
    
    NAME                                 DESIRED   CURRENT   READY   AGE
    replicaset.apps/gateway-868759df7d   1         1         1       63m
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-infra$


## Testing inter-cluster communication between gateway and Keycloak

For more information about the integration of Keycloak read [README.md](https://github.com/borispopicbusiness/orvix-infra/blob/develop/environments/dev/keycloak/README.md) from orvix-infra repository.

I will not repeat myself but as shown in the referenced [README.md](https://github.com/borispopicbusiness/orvix-infra/blob/develop/environments/dev/keycloak/README.md) file the endpoint responsible for processing
diagnostics requests used for listing available services and the shown services by executing `keycloak get svc -m dev` are identical.
And that is the expected behavior.

During the setup, I installed [MetalLB](https://github.com/borispopicbusiness/orvix-infra/tree/develop/environments/misc/metallb) and configured an [IP pool](https://github.com/borispopicbusiness/orvix-infra/blob/develop/environments/misc/metallb/metallb-values.yaml), which resolved the ISS issue with Keycloak.
The root cause was that the system did not maintain consistent ISS fields in JWT tokens for external and internal traffic.
Our services are now externally exposed as shown below:


    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ kubectl get ingress -n dev
    NAME                     CLASS     HOSTS                               ADDRESS         PORTS   AGE
    gateway                  traefik   gateway.dev.k8s-svc.homelab         192.168.1.242   80      21h
    keycloak-dev-keycloakx   traefik   keycloak-dev.keycloak.example.com   192.168.1.242   80      7h28m
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ 


To demonstrate that the iss problem is successfully resolved I will provide as proof two results generated for that purpose. These results show
the decoded values from two JWT tokens, one internal and one external.

The external iss value:

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ kubectl get ingress -n dev
    NAME                     CLASS     HOSTS                               ADDRESS         PORTS   AGE
    gateway                  traefik   gateway.dev.k8s-svc.homelab         192.168.1.242   80      21h
    keycloak-dev-keycloakx   traefik   keycloak-dev.keycloak.example.com   192.168.1.242   80      7h28m
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ curl -X POST \
        -d "grant_type=password" \
        -d "client_id=orvix-gateway-dev-local" \
        -d "username=user-dev-local" \
        -d "password=develop" \
        -d "scope=openid" \
        http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm/protocol/openid-connect/token | \
        jq -r '.access_token' | \
        cut --delimiter='.' -f2 | \
        base64 --decode | \
        jq -r ".iss"
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
    Dload  Upload   Total   Spent    Left  Speed
    100  3684  100  3577  100   107  34680   1037 --:--:-- --:--:-- --:--:-- 35766
    http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ 

As we can see the external iss stores `http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm`.

The internal iss value:

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ kubectl get pods -n dev
    NAME                       READY   STATUS    RESTARTS     AGE
    gateway-68f66bbf88-rqb4p   1/1     Running   4 (9h ago)   22h
    keycloak-dev-keycloakx-0   1/1     Running   0            8h
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ kubectl exec -it gateway-68f66bbf88-rqb4p -n dev -- bash
    root@gateway-68f66bbf88-rqb4p:/app# curl -X POST \
        -d "grant_type=password" \
        -d "client_id=orvix-gateway-dev-local" \
        -d "username=user-dev-local" \
        -d "password=develop" \
        -d "scope=openid" \
        http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm/protocol/openid-connect/token | \
        jq -r '.access_token' | \
        cut --delimiter='.' -f2 | \
        base64 --decode | \
        jq -r ".iss"
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
    Dload  Upload   Total   Spent    Left  Speed
    100  3684  100  3577  100   107  22933    686 --:--:-- --:--:-- --:--:-- 24078
    http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm
    root@gateway-68f66bbf88-rqb4p:/app#

The internal iss value is `http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm`. They are identical

And here is the response from a successfully processed `GET` request to `/api/v1/diagnostics/cloud/services/all`

    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ curl -H "Host: gateway.dev.k8s-svc.homelab" \
        -H "Authorization: Bearer <access-token>" \
        http://gateway.dev.k8s-svc.homelab/api/v1/diagnostics/cloud/services/all | jq
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
    Dload  Upload   Total   Spent    Left  Speed
    100    75  100    75    0     0   2694      0 --:--:-- --:--:-- --:--:--  2777
    [
        "gateway",
        "keycloak-dev-keycloakx-headless",
        "keycloak-dev-keycloakx-http"
    ]
    boris@boris-Nitro-AN515-58:~/core-repos/orvix/orvix-gateway$ 

## Testing the global filter that examines JWT tokens, generates a correlation id, and sends a new package downstream

Here I will demonstrate how the global filter that examines JWT tokens, generates a correlation id, and sends a new package downstream.
For this purpose we need **nc** because we need a simple web server to act as the downstream the downstream's termination point that receives requests and lets us examine the headers and bodies of the received packages.

Start **nc** on port **8080**:
```bash
nc -lv 8080
```

First, we need an access token. We can obtain one by executing the following curl command in the local shell:
```yaml
curl -X POST \
    -d "grant_type=password" \
    -d "client_id=orvix-gateway-dev-local" \
    -d "username=user-dev-local" \
    -d "password=develop" \
    -d "scope=openid" \
    http://keycloak-dev.keycloak.example.com/auth/realms/orvix-realm/protocol/openid-connect/token | \
    jq -r '.access_token'
```

The command above sends a request to Keycloak and uses jq to extract the access_token from the JSON response.

Copy the returned access token and replace <access-token> in the following command with it:
```bash
curl -H "Host: gateway.dev.k8s-svc.homelab" \
    -H "Authorization: Bearer <access-token>" \
    http://gateway.dev.k8s-svc.homelab/api/v1/diagnostics/gateway/services/all | jq
```

The request is sent to the gateway with the JWT access token in the Authorization header. The global filter processes the JWT, generates a correlation ID, and forwards the request downstream.

The nc process listening on port 8080 allows us to observe the HTTP request that the gateway sends to the downstream service.
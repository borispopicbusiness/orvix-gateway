# Orvix Gateway

**Orvix Gateway** is a Spring Boot based microservice using Spring Cloud and WebFlux which relies on **Project Reactor**.

⚠️⚠️⚠️ Caution: This is just the beginning. The gateway is not yet functional; development is ongoing.

## Tech stack

    - Java 21
    - maven 3.9.5
    - Spring Boot 3.3.4

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

## Microservice Endpoints

The microservice exposes the following Actuator endpoints, which can be used for health checks and informational purposes in a Kubernetes environment:

| Endpoint               | Description                                   |
|------------------------|-----------------------------------------------|
| **`/actuator/health`** | Returns the health status of the application  |
| **`/actuator/info`**   | Provides general application information      |

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
helm upgrade --install ./helm/gateway/ -f ./helm/gateway/values-dev.yaml --namespace dev
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


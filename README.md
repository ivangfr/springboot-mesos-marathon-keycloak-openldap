# springboot-mesos-marathon-keycloak-openldap

The goal of this project is to create a simple [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) REST API, called `simple-service`, and secure it with [`Keycloak`](https://www.keycloak.org). The API users will be loaded from [`OpenLDAP`](https://www.openldap.org) server. Furthermore, we will start [`Mesos`](http://mesos.apache.org/) / [`Marathon`](https://mesosphere.github.io/marathon) environment, so that we can deploy `Keycloak` and `simple-service` in it.

## Application

- ### simple-service

  `Spring Boot` Java Web application that exposes two endpoints:
  - `/api/public`: endpoint that can be access by anyone, it is not secured;
  - `/api/private`: endpoint that can just be accessed by users that provides a `JWT` token issued by `Keycloak` and the token must contain the role `USER`.

## Prerequisites

- [`Java 11+`](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [`Docker`](https://www.docker.com/)
- [`Docker-Compose`](https://docs.docker.com/compose/install/)

## Warning: Mac Users

   After some recent `Docker Desktop` updates, we need to add a new directory called `/var/lib` to Docker `File Sharing` resources, otherwise we will see an exception like
   ```
   docker: Error response from daemon: Mounts denied: 
   The path /var/lib/mesos/slaves/347adc13-aad3-4c25-9864-ee2ebcc97572-S0/frameworks/347adc13-aad3-4c25-9864-ee2ebcc97572-0000/executors/keycloak.05642772-1ae3-11eb-aecf-0242ac120007/runs/33e0751e-1a0a-40d8-b9f8-cfa706679172
   is not shared from OS X and is not known to Docker.
   You can configure shared paths from Docker -> Preferences... -> File Sharing.
   See https://docs.docker.com/docker-for-mac/osxfs/#namespaces for more info.
   ```

   Unfortunately, it's not possible to do it by using `Docker Desktop` UI. So, we need to do it manually by following the next steps:
   - Open `~/Library/Group\ Containers/group.com.docker/settings.json` using your favorite editor;
   - At the top of the file you will see an array that looks like this:
     ```
     "filesharingDirectories" : [
       "\/Users",
       "\/Volumes",
       "\/private",
       "\/tmp"
     ],
     ```
   - Append the following line:
     ```
     "\/var\/lib"
     ```
   - The new array should now look like the one below (mind the comma after `"\/tmp"`):
     ```
     "filesharingDirectories" : [
       "\/Users",
       "\/Volumes",
       "\/private",
       "\/tmp",
       "\/var\/lib"
     ],
     ```
   - Save the file and exit;
   - Restart `Docker Desktop`.

## Start Environment

- Open a terminal and make sure you are in `springboot-mesos-marathon-keycloak-openldap` root folder

- Export to an environment variable called `HOST_IP_ADDR` the machine ip address. It can be obtained by executing `ifconfig` command on Mac/Linux terminal or `ipconfig` on Windows.
  ```
  export HOST_IP_ADDR=...
  ```

- Then, run the following command
  ```
  docker-compose up -d
  ```

- Wait a bit until `zookeeper`, `mysql-keycloak` and `mesos-master` containers are Up (healthy). In order to check it run
  ```
  docker-compose ps
  ```

## Service's URL

| Service  | URL                   |
| -------- | --------------------- |
| Mesos    | http://localhost:5050 |
| Marathon | http://localhost:8090 |

## Import OpenLDAP Users

The `LDIF` file that we will use, `springboot-mesos-marathon-keycloak-openldap/ldap/ldap-mycompany-com.ldif`, contains already a pre-defined structure for `mycompany.com`. Basically, it has 2 groups (`developers` and `admin`) and 4 users (`Bill Gates`, `Steve Jobs`, `Mark Cuban` and `Ivan Franchin`). Besides, it's defined that `Bill Gates`, `Steve Jobs` and `Mark Cuban` belong to `developers` group and `Ivan Franchin` belongs to `admin` group.
```
Bill Gates > username: bgates, password: 123
Steve Jobs > username: sjobs, password: 123
Mark Cuban > username: mcuban, password: 123
Ivan Franchin > username: ifranchin, password: 123
```

To import those users to `OpenLDAP`

- In a terminal, make sure you are in `springboot-mesos-marathon-keycloak-openldap` root folder
- Run the following script
  ```
  ./import-openldap-users.sh
  ```

## Build simple-service Docker Image

- In a terminal, make sure you are in `springboot-mesos-marathon-keycloak-openldap` root folder

- Run the following command to build `simple-service` Docker Image
  ```
  ./mvnw clean compile jib:dockerBuild --projects simple-service
  ```

## Deploy Keycloak to Marathon

- In a terminal and inside `springboot-mesos-marathon-keycloak-openldap` root folder, run
  ```
  curl -X POST \
    -H "Content-type: application/json" \
    -d @./marathon/keycloak.json \
    http://localhost:8090/v2/apps
  ```

- Open [`Marathon` website](http://localhost:8090) and wait for `Keycloak` to be healthy

- You can monitor `Keycloak` deployment logs on [`Mesos` website](http://localhost:5050)

  ![mesos](images/mesos.png)

  - On `Active Tasks` section, find the task `keycloak` and click on `Sandbox` (last link on the right)
  - Then, click on `stdout`
  - A window will open, and the logs will be displayed real-time

## Getting Keycloak Address

When `Keycloak` is deployed in `Marathon`, it's assigned to it a host and port. There are two ways to obtain it

1. Running the following command in a terminal 
   ```
   KEYCLOAK_ADDR="$(curl -s http://localhost:8090/v2/apps/keycloak | jq -r '.app.tasks[0].host'):$(curl -s http://localhost:8090/v2/apps/keycloak | jq '.app.tasks[0].ports[0]')"
   
   echo $KEYCLOAK_ADDR
   ```

1. Using [`Marathon` website](http://localhost:8090)

## Configuring Keycloak

Keycloak can be configured by running a script or manually. For manual configuration check [`Configure Keycloak Manually`](https://github.com/ivangfr/springboot-mesos-marathon-keycloak-openldap/blob/master/configure-keycloak-manually.md). Below, it's explained to configure by running a script.

- In a terminal, make sure you are in `springboot-mesos-marathon-keycloak-openldap` root folder

- Get [`Keycloak` address](#getting-keycloak-address). The environment variables `KEYCLOAK_ADDR` must have `Keycloak` host and port
  
- Run the following script
  ```
  ./init-keycloak.sh $KEYCLOAK_ADDR
  ```
  This script creates `company-services` realm, `simple-service` client, `USER` client role, `ldap` federation and the users `bgates` and `sjobs` with the role `USER` assigned.
  
## Deploy simple-service to Marathon

- Get [`Keycloak` address](#getting-keycloak-address)

- Update the property `env.keycloak.auth-server-url` that is present in `marathon/simple-service.json`, informing `Keycloak` address 

- In a terminal and inside `springboot-mesos-marathon-keycloak-openldap` root folder, run
  ```
  curl -X POST http://localhost:8090/v2/apps \
    -H "Content-type: application/json" \
    -d @./marathon/simple-service.json
  ```

- Open [`Marathon` website](http://localhost:8090) and wait for `simple-service` to be healthy. You can monitor `simple-service` deployment logs on [`Mesos` website](http://localhost:5050)

- The figure below shows `keycloak` and `simple-service` running on `Marathon`

  ![marathon](images/marathon.png)

## Getting simple-service address

When `simple-service` is deployed in `Marathon`, it's assigned to it a host and port. There are two ways to obtain it

1. Running the following command in a terminal 
   ```
   SIMPLE_SERVICE_ADDR="$(curl -s http://localhost:8090/v2/apps/simple-service | jq -r '.app.tasks[0].host'):$(curl -s http://localhost:8090/v2/apps/simple-service | jq '.app.tasks[0].ports[0]')"
   
   echo $SIMPLE_SERVICE_ADDR
   ```
  
1. Using [`Marathon`](http://localhost:8090)

## Testing simple-service

- In a terminal, make sure you have the environment variables [`KEYCLOAK_ADDR`](#getting-keycloak-address) and [`SIMPLE_SERVICE_ADDR`](#getting-simple-service-address) with the host and port of `Keycloak` and `simple-service` respectively

- Try to access `GET /api/public` endpoint
  ```
  curl -i "http://$SIMPLE_SERVICE_ADDR/api/public"
  ```
  It should return
  ```
  HTTP/1.1 200
  It is public.
  ```

- Access `GET /api/private` endpoint (without authentication)
  ```
  curl -i "http://$SIMPLE_SERVICE_ADDR/api/private"
  ```
  It should return
  ```
  HTTP/1.1 302
  ```
  > Here, the application is trying to redirect the request to an authentication link.

- Get `bgates` access token
  ```
  BGATES_ACCESS_TOKEN=$(curl -s -X POST \
    "http://$KEYCLOAK_ADDR/auth/realms/company-services/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=bgates" \
    -d "password=123" \
    -d "grant_type=password" \
    -d "client_id=simple-service" | jq -r .access_token)
  
  echo $BGATES_ACCESS_TOKEN
  ```

- Access `GET /api/private` endpoint this time, informing the access token
  ```
  curl -i -H "Authorization: Bearer $BGATES_ACCESS_TOKEN" "http://$SIMPLE_SERVICE_ADDR/api/private"
  ```
  It should return
  ```
  HTTP/1.1 200
  bgates, it is private.
  ```

## Shutdown

- Go to `Marathon` and click on `simple-service` application
- On the next page, click on the `gear` symbol and then on `Destroy`
- Confirm the destruction of the application
- Do the same for `keycloak` application

- After that, go to a terminal and, inside `springboot-mesos-marathon-keycloak-openldap` root folder, run
  ```
  docker-compose down -v
  docker rm -v $(docker ps -a -f status=exited -f status=created -q)
  ```

- Undo changes in `~/Library/Group\ Containers/group.com.docker/settings.json` file
  - Open `~/Library/Group\ Containers/group.com.docker/settings.json` using your favorite editor;
  - Remove `"\/var\/lib"` of the `filesharingDirectories` array present at the top of file;
  - Restart `Docker Desktop`.
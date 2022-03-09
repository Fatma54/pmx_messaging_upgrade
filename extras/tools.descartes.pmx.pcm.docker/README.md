# PcmConsoleWebServer
A webserver using spring.io to deploy the PmxConsole project in a Docker container thus enabeling the PcmConsole to run on a remote server that will automaticaly
download and analyze the data and returns the results in a zip file.

<br><br>

## Build the Project and Docker Image

### Build the java webserver
To build the Webserver jar application using maven call 
```
mvn deploy
```
the resulting executable jar file will be placed in the target folder.

### Build the Docker Image
The docker image is built with
```
(sudo) docker build -t descartesresearch/pmx-pcm-server .
```

## Docker-Hub
The docker image is available also at DockerHUB to pull the image just call:

```
(sudo) docker pull descartesresearch/pmx-pcm-server 
```

<br><br>

## Running the Webserver
The webserver is based on spring.io and java

## Runing the Docker Image
with the following command the docker image will start and the port 8080 of the localhost will exposed to the docker port 8080 of the Spring.io server
```
docker run -d -p 8080:8080 descartesresearch/pmx-pcm-server
```
Now you can access the webserver with localhost:8080


### Accsessing other application on HOST
Docker runs each image with its own network thus meaning that request to localhost will be sent to the docker image itself an not to the host running Docker.
To solve this the requests have to be to the IP address of the Host. 

However it is possible ro run docker in host mode with the command ```docker run --net="host"``` this will start the docker image with the network setting set to host.
the containerwill schare the network stack of the docker host and localhost will now refer to the host not the docker containter.
If running on "Host" mode it is not requierd to run docker with the port commane ```-p 8080:8080``` since the docker will automatacaly open the port 8080 to the host.


### Available Parameters:
* --debug=true :will result in Debug mode and the console wil output a lot of information in the console
* --core="--cores="edge-uq38n=4, middletier-64bqq=4" : needed for pmx specifies the number of cores used

on the localhost the webserver should now be reachable under
```
localhost:8080
```
There you can upload a zip-file to analyze or by providing the server with a url to download the data

```
localhost:8080/url/?id=<SERVERADDRESS_THAT_RETURNS_ZIP/bookstore.zip
```
The Server will then download the zipfile from the server Address specified unpack it and pmxConsole will then analyse the data. A result.zip file will then returend to download with the results.


Calling the following command will retrive the last results of the docker image
```
localhost:8080/url/?getZip
```
<br><br>


## Spring.IO and Maven
For more Information and tutorials using Spring.io and Maven please visit
```
http://docs.spring.io/spring-boot/docs/current/reference/html/getting-started-first-application.html
```


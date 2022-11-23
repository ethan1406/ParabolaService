# ParabolaService

Build the server by running 
`docker build --build-arg JAR_FILE=build/libs/\*.jar -t parabola:latest .`

To connect to a mysql server, the server exposes the following environment variables:
`DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD`.

We recommend pulling the official mysql docker image and running it locally. Instructions can be found here:
https://hub.docker.com/_/mysql. 

After starting the mysql sever (the default user is `root`), we can run the parabola service by passing 
mysql server info into env variables. An example command is: 
` docker run -d -p50051:50051 --env DB_PORT=3306 --env DB_HOST=localhost 
-e DB_NAME=parabola -e DB_PASSWORD=mypassword -e DB_USER=admin -e parabola:latest`

After starting the parabola service, start the envoy proxy service at: https://github.com/ethan1406/parabola-envoy.

At the end there should be three docker containers running. 1. Database 2. parabola service 3. proxy server
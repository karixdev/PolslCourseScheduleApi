# PolslCourseScheduleApi

## Project description

- The aim of the project was to build a publicly accessible course schedule REST API for Silesian University of Technology's full-time majors.
- This is a private project, not supported by the university. 
- Motivation: 
  - Lack of a publicly available REST API of the university's course schedule - project web scrapes schedules from [plan.polsl.pl](https://plan.polsl.pl/). 
  - The official schedule page works quite slowly - every time we request for another schedule half of the page is re-rendered and all front-end processing (assigning classes to HTML elements, etc.) is done on the server side, not on the client side.
- The courses are cyclically updated - by default every hour.

## Architecture
<img src="https://github.com/karixdev/PolslCourseScheduleApi/blob/v2-dev/images/architecture.svg" width="100%"/>

## Current problems and future improvements
Currently, the biggest problem is the Silesian University of Technology server rejecting the TCP/IP connection when the web scraping microservice is running in a Docker container. This problem makes it impossible to put the entire application in the Docker containers.

For the current time I am looking for a solution. When I do manage to find a fix I will place the whole application in Docker containers so that the microservices do not have to be started manually.

## Requirements
- `Java (JDK) 17`
- `Docker`

## How to use it
To start the application, first run the command:
```shell
docker compose up -d
```
And then start all microservices except `discovery-service`, in order so that the `api-gateway` is started last.

The application exposes a REST interface accessible via the API gateway available at `localhost:9090`.
For a better overview of the application, it is recommended to first use the swagger available at `localhost:9090/swagger-ui.html`.

All endpoints except those accessible via the `GET` method are secured - the exception is the webhook-service microservice, there all endpoints are secured.
For authentication, use Keycloak. There is available test client:
- Client ID: `test-client`
- Client Secret: `i2hDNLcuCBOHSBedlGELOp6RFvVQY4cc`
- Scope: `openid`
- Available users:
  - username: `admin` password: `admin`
  - username: `user` password: `user`

### Note
To obtain schedule `planPolslId`, `type`, `wd` you need to go to [plan.polsl.pl](https://plan.polsl.pl/), find the schedule you are interested in right click on it and select "Inspect element". In the developer tools window you will see a link to this schedule, it should look similar to this example:  `"plan.polsl.pl/plan.php?type=0&id=39884&wd=4"`, the parameters in this link correspond to the following:
- `id` - `planPolslId`
- `type` - `type`
- `wd` - `wd`

For example:
- Inf II 1/1's schedule has `id` equal to `18843`, `type` equal to `0` and `wd` equal to 4

Management endpoints:
- RabbitMQ Management
  - port: `15672`
  - username: `user`
  - password: `password`
- Keycloak admin
  - port: `8080`
  - username: `admin`
  - password: `admin`

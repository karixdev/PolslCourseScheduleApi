# PolslCourseScheduleApi

## Project description

- The aim of the project was to build a publicly accessible course schedule REST API for Silesian University of Technology's full-time majors.
- This is a private project, not supported by the university. 
- Motivation: 
  - Lack of a publicly available REST API of the university's course schedule - project web scrapes schedules from [plan.polsl.pl](https://plan.polsl.pl/). 
  - The official schedule page works quite slowly - every time we request for another schedule half of the page is re-rendered and all front-end processing (assigning classes to HTML elements, etc.) is done on the server side, not on the client side.
- The courses are cyclically updated - by default every hour.

## Requirements to run project
- `Docker`

For development:
- `Docker`
- `Java 17`
- `Maven 3`

## How to use it

```shell
docker compose up -d
```

Application has `3` Docker profiles:
- `default` (you don't have to specify any profile in command):
  - Zookeeper
  - Kafka broker
  - AKHQ
  - Postgres for Keycloak
  - Keycloak
  - Jaeger
  - Postgres for schedule-service
  - Postgres for course-service
- `infrastructure-services`:
  - All services from `default` profile
  - discovery-server - Eureka server for microservice discovery
  - api-gateway - gateway for routing incoming requests
- `application-services`: 
  - All services from `default` and `infrastructure-services` profiles
  - schedule-service - holds available schedules data
  - course-service - holds schedule courses data
  - domain-model-mapper-service - maps raw web scraped data into domain models
  - web-scraper-service - web scrapes data from plan.polsl.pl

If you're going to use `application-services` profile then you have to add following record to `hosts` file in your OS: `127.0.01 keycloak`

To access `swagger` visit `localhost:8080/swagger-ui.html`. If there's a problem while retrieving `OpenAPI` specification from `course-service` or `schedule-service`, restart `api-gateway` container.

The application exposes a REST interface accessible via the API gateway available at `localhost:9090`.
For a better overview of the application, it is recommended to first use the swagger available at `localhost:9090/swagger-ui.html`.

Keycloak test client data:
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

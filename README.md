
# DDD Microservices Example Project in Java: E-Commerce)

The purpose of this project is to provide a sample implementation of an e-commerce product following **Domain-Driven Design (DDD)** and **Service-Oriented Architecture (SOA)** principles.

Programming language is Java 11 with heavy use of Spring framework, Docker and Kubernetes.

## Purpose of the Project

This repository focuses mostly on cross-cutting, infrastructure and deployment concerns. 

For the domain and application concepts see the [original repository](https://github.com/ttulka/ddd-example-ecommerce). 

## !!! WORK IN PROGRESS !!!

Planed work:

- External messaging with Redis :white_check_mark:
- External database PostgreSQL :white_check_mark:
- Maven to Gradle migration :white_check_mark:
- Services to microservices with Spring Boot :white_check_mark:
- Microservices as Docker images :white_check_mark:
- Docker-Compose for local development
- Integration tests module
- Kubernetes cluster
- Monitoring
- Security

## Message Broker

As the message broker a simple **Redis** instance could be used:

```
docker run --rm --name redis-broker -p 6379:6379 -d redis:6 redis-server
```

Start the application with Spring profile `redis`:

```
gradle bootRun --args='--spring.profiles.active=redis' -b application/build.gradle
```

When the `redis` profile is not active, the system will fall-back to use of Spring application events as the default messaging mechanism.

### Messaging Integration

To make the code independent of a concrete messaging implementation and easy to use, Spring application events are used for the internal communication.

In practice, this means that messages are published via `EventPublisher` abstraction and consumed via Spring's `@EventListener`.

To make this work, the external messages are re-sent as Spring application events under the hood.   

## Database

The whole system uses one externalized database with particular tables owned exclusively by services.

In a real-world system this separation would be further implemented by separate schemas/namespaces.

As the database **PostgreSQL** instance could be used:

```
docker run --rm --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=secret -d postgres:13
```

Start the application with Spring profile `postgres`:

```
gradle bootRun --args='--spring.profiles.active=postgres' -b application/build.gradle
```

When the `postgres` profile is not active, the system will fall-back to use H2 as the default database.

## Gradle Build 

Every microservice must be build separately.

First, publish common dependencies:
```
gradle build publishToMavenLocal -b common/build.gradle
```

Then, publish public APIs of services:
``` 
gradle events:build events:publishToMavenLocal -b billing/payment/build.gradle
gradle events:build events:publishToMavenLocal -b sales/order/build.gradle
gradle events:build events:publishToMavenLocal -b shipping/delivery/build.gradle
gradle events:build events:publishToMavenLocal -b shipping/dispatching/build.gradle
gradle events:build events:publishToMavenLocal -b warehouse/build.gradle
```

Afterwards, build and publish the service:
``` 
gradle build publishToMavenLocal -b sales/catalog/build.gradle
gradle build publishToMavenLocal -b sales/cart/build.gradle 
gradle build publishToMavenLocal -b sales/order/build.gradle 
gradle build publishToMavenLocal -b billing/payment/build.gradle
gradle build publishToMavenLocal -b shipping/delivery/build.gradle
gradle build publishToMavenLocal -b shipping/dispatching/build.gradle
gradle build publishToMavenLocal -b warehouse/build.gradle
```

Build the portal:
```
gradle build publishToMavenLocal -b portal/build.gradle
```

Run the monolithic application:
```
gradle test bootRun -b application/build.gradle
```

Alternatively, start as a set of microservices:
```
gradle application:bootRun -b sales/catalog/build.gradle
gradle application:bootRun -b sales/order/build.gradle
gradle application:bootRun -b sales/cart/build.gradle
gradle application:bootRun -b billing/payment/build.gradle
gradle application:bootRun -b shipping/delivery/build.gradle
gradle application:bootRun -b shipping/dispatching/build.gradle
gradle application:bootRun -b warehouse/build.gradle
``` 

## Docker Containers

Build an image per microservice:
```
docker build -t ttulka/ecommerce-catalog-service sales/catalog/application
docker build -t ttulka/ecommerce-order-service sales/order/application
docker build -t ttulka/ecommerce-cart-service sales/cart/application
docker build -t ttulka/ecommerce-payment-service billing/payment/application
docker build -t ttulka/ecommerce-delivery-service shipping/delivery/application
docker build -t ttulka/ecommerce-dispatching-service shipping/dispatching/application
docker build -t ttulka/ecommerce-warehouse-service warehouse/application
```

Alternatively, the same can be achieved via Gradle:
```
gradle application:bootBuildImage --imageName=ttulka/ecommerce-catalog-service -b sales/catalog/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-order-service -b sales/order/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-cart-service -b sales/cart/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-payment-service -b billing/payment/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-delivery-service -b shipping/delivery/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-dispatching-service -b shipping/dispatching/build.gradle
gradle application:bootBuildImage --imageName=ttulka/ecommerce-warehouse-service -b warehouse/build.gradle
```

Run the containers:
```
docker container run --rm -p 8080:8001 ttulka/ecommerce-catalog-service
docker container run --rm -p 8080:8002 ttulka/ecommerce-order-service
docker container run --rm -p 8080:8003 ttulka/ecommerce-cart-service
docker container run --rm -p 8080:8004 ttulka/ecommerce-payment-service
docker container run --rm -p 8080:8005 ttulka/ecommerce-delivery-service
docker container run --rm -p 8080:8006 ttulka/ecommerce-dispatching-service
docker container run --rm -p 8080:8007 ttulka/ecommerce-warehouse-service
```

Active profiles can be set as follows:
```
docker container run --rm -e "SPRING_PROFILES_ACTIVE=redis,postgres" -p 8080:8001 ttulka/ecommerce-catalog-service
```
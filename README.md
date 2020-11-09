
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
- Microservices as Docker images
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
gradle build publishToMavenLocal -b warehouse/build.gradle
gradle build publishToMavenLocal -b shipping/delivery/build.gradle
gradle build publishToMavenLocal -b shipping/dispatching/build.gradle
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
gradle application:bootRun -b warehouse/build.gradle
gradle application:bootRun -b shipping/delivery/build.gradle
gradle application:bootRun -b shipping/dispatching/build.gradle
``` 
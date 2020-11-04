# DDD Microservices Example Project in Java: E-Commerce)

The purpose of this project is to provide a sample implementation of an e-commerce product following **Domain-Driven Design (DDD)** and **Service-Oriented Architecture (SOA)** principles.

Programming language is Java 12 with heavy use of Spring framework, Docker and Kubernetes.

## !!! WORK IN PROGRESS !!!

Planed work:

- External messaging with Redis :white_check_mark:
- External database PostgreSQL :white_check_mark:
- Maven to Gradle migration
- Services to microservices with Spring Boot
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
mvn spring-boot:run -Dspring-boot.run.profiles=redis
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
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

When the `postgres` profile is not active, the system will fall-back to use H2 as the default database.

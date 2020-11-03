# DDD Microservices Example Project in Java: E-Commerce)

The purpose of this project is to provide a sample implementation of an e-commerce product following **Domain-Driven Design (DDD)** and **Service-Oriented Architecture (SOA)** principles.

Programming language is Java 12 with heavy use of Spring framework, Docker and Kubernetes.

!!! WORK IN PROGRESS !!!


## Message Broker

As the message broker a simple **Redis** instance could be used:

```
docker run --rm --name redis-broker -p 6379:6379 -d redis:6 redis-server
```

Start the application with Spring profile `redis`:

```
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```



# DDD Microservices Example Project in Java: E-Commerce)

The purpose of this project is to provide a sample implementation of an e-commerce product following **Domain-Driven Design (DDD)** and **Service-Oriented Architecture (SOA)** principles.

Programming language is Java 11 with heavy use of Spring framework, Docker and Kubernetes.

## Purpose of the Project

This repository focuses mostly on cross-cutting, infrastructure and deployment concerns. 

For the domain and application concepts see the [original repository](https://github.com/ttulka/ddd-example-ecommerce).

## Monolith vs Microservices

Both monolithic and microservices deployments are implemented. 

Run the monolithic application:

```sh
gradle :application:bootRun
``` 

## Message Broker

As the message broker a simple **Redis** instance could be used:

```sh
docker run --rm --name redis-broker -p 6379:6379 -d redis:6 redis-server
```

Start the application with Spring profile `redis`:

```sh
gradle :application:bootRun --args='--spring.profiles.active=redis'
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

```sh
docker run --rm --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=secret -d postgres:13
```

Start the application with Spring profile `postgres`:

```sh
gradle :application:bootRun --args='--spring.profiles.active=postgres'
```

When the `postgres` profile is not active, the system will fall-back to use H2 as the default database.

## Gradle Build 

The project is a Gradle multi-project, all sub-projects can be built in a single command:

```sh
gradle build
```

## Docker Containers

Build an image per microservice via Gradle Spring Boot plugin:
```sh
gradle bootBuildImage
```

To run the containers:
```sh
docker container run --rm -p 8080:8001 ttulka/ecommerce-catalog-service
docker container run --rm -p 8080:8002 ttulka/ecommerce-order-service
docker container run --rm -p 8080:8003 ttulka/ecommerce-cart-service
docker container run --rm -p 8080:8004 ttulka/ecommerce-payment-service
docker container run --rm -p 8080:8005 ttulka/ecommerce-delivery-service
docker container run --rm -p 8080:8006 ttulka/ecommerce-dispatching-service
docker container run --rm -p 8080:8007 ttulka/ecommerce-warehouse-service
docker container run --rm -p 8080:8000 ttulka/ecommerce-portal-service
```

Active profiles can be set as follows:
```sh
docker container run --rm -e "SPRING_PROFILES_ACTIVE=redis,postgres" -p 8080:8001 ttulka/ecommerce-catalog-service
```

### Docker-Compose

Build NGINX reverse proxy image:
```sh
docker build -t ttulka/ecommerce-reverseproxy reverseproxy
```

Start the entire microservices stack:
```sh
docker-compose up
```

Access the Postgres database and init some data:
```sh
docker exec -it <containerID> psql -U postgres postgres
```

```sql
INSERT INTO categories VALUES
    ('1', 'books', 'books'),
    ('2', 'games-toys', 'games and toys'),
    ('3', 'others', 'others');

INSERT INTO products VALUES
    ('1', 'Domain-Driven Design', 'by Eric Evans', 45.00),
    ('2', 'Object Thinking', 'by David West', 35.00),
    ('3', 'Release It!', 'by Michael Nygard', 32.50),
    ('4', 'Chess', 'Deluxe edition of the classic game.', 3.20),
    ('5', 'Domino', 'In black or white.', 1.50),
    ('6', 'Klein bottle', 'Two-dimensional manifold made from glass.', 25.00);

INSERT INTO products_in_categories VALUES
    ('1', '1'),
    ('2', '1'),
    ('3', '1'),
    ('4', '2'),
    ('5', '2'),
    ('6', '3');

INSERT INTO products_in_stock VALUES
    ('1', 5),
    ('2', 0),
    ('3', 13),
    ('4', 55),
    ('5', 102),
    ('6', 1);
```

The NGINX reverse proxy serves as a simple API gateway:
```sh
curl localhost:8080/catalog/products
curl localhost:8080/warehouse/stock/5
```

## Kubernetes

To use local images for development with Minikube, run the following command to use local Docker images registry:
```sh
minikube start
eval $(minikube docker-env)
```

Afterwards, build the docker images again for the Minikube's Docker daemon:
```sh
gradle bootBuildImage
```

Create deployments:
```sh
kubectl apply -f 1-infrastructure.k8s.yml
kubectl apply -f 2-backend-services.k8s.yml
kubectl apply -f 3-frontend-portal.k8s.yml
kubectl apply -f 4-api-gateway.k8s.yml
```

Set up ports forwarding to access the cluster from your local network:
```sh
kubectl port-forward service/reverseproxy 8080:8080
```

Alternatively, you can create an Ingress:
```sh
minikube addons enable ingress

kubectl apply -f 5-ingress.k8s.yml

# get the ingress address 
kubectl get ingress ecommerce-ingress

# add the address into hosts
sudo cp /etc/hosts hosts.bak
sudo echo -e '\n<ingress-address> ecommerce.local' >> /etc/hosts

# access the application in browser: http://ecommerce.local
``` 
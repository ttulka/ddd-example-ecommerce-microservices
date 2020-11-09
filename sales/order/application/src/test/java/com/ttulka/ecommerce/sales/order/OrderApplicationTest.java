package com.ttulka.ecommerce.sales.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.with;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void order_is_placed() {
        with()
                .port(port)
                .basePath("/order")
                .contentType(ContentType.JSON)
                .body("{" +
                      "\"orderId\": \"order-1\"," +
                      "\"total\": 123.5," +
                      "\"items\": [{" +
                      "\"productId\": \"p-1\"," +
                      "\"quantity\": 5" +
                      "}]" +
                      "}")
                .post()
                .then()
                .statusCode(201);
    }
}

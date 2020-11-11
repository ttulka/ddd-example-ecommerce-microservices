package com.ttulka.ecommerce.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.with;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PortalApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void index_works() {
        with().log().ifValidationFails()
                .port(port)
                .basePath("/")
                .then()
                .statusCode(200);
    }

    @Test
    void JS_resource_works() {
        with().log().ifValidationFails()
                .port(port)
                .basePath("/js/Application.js")
                .then()
                .statusCode(200);
    }
}

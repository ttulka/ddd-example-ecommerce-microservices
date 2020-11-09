package com.ttulka.ecommerce.sales.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void item_is_added() {
        CookieFilter cookieFilter = new CookieFilter(); // share cookies among requests

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .contentType(ContentType.JSON)
                .body("{" +
                      "\"productId\": \"p-1\"," +
                      "\"title\": \"product 1\"," +
                      "\"price\": 12.3," +
                      "\"quantity\": 123" +
                      "}")
                .post()
                .then()
                .statusCode(201);

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .get()
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void item_is_removed() {
        CookieFilter cookieFilter = new CookieFilter(); // share cookies among requests

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .contentType(ContentType.JSON)
                .body("{" +
                      "\"productId\": \"p-2\"," +
                      "\"title\": \"product 2\"," +
                      "\"price\": 12.3," +
                      "\"quantity\": 123" +
                      "}")
                .post()
                .then()
                .statusCode(201);

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .param("productId", "p-2")
                .delete()
                .then()
                .statusCode(200);

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .get()
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void cart_is_emptied() {
        CookieFilter cookieFilter = new CookieFilter(); // share cookies among requests

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .contentType(ContentType.JSON)
                .body("{" +
                      "\"productId\": \"p-2\"," +
                      "\"title\": \"product 2\"," +
                      "\"price\": 12.3," +
                      "\"quantity\": 123" +
                      "}")
                .post()
                .then()
                .statusCode(201);

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart/empty")
                .post()
                .then()
                .statusCode(200);

        with()
                .filter(cookieFilter)
                .port(port)
                .basePath("/cart")
                .get()
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }
}

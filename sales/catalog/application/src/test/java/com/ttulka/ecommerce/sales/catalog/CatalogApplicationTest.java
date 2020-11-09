package com.ttulka.ecommerce.sales.catalog;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class CatalogApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    @Sql(statements = "INSERT INTO categories VALUES ('C1', 'uri-1', 'Category 1')")
    void categories_are_listed() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/catalog/categories")
                .get()
                .andReturn()
                .jsonPath().getMap("[0]");

        assertAll(
                () -> assertThat(payment.get("uri")).isEqualTo("uri-1"),
                () -> assertThat(payment.get("title")).isEqualTo("Category 1"));
    }

    @Test
    @Sql(statements = "INSERT INTO products VALUES ('P1', 'Product 1', 'Description 1', 123.4)")
    void products_are_listed() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/catalog/products")
                .get()
                .andReturn()
                .jsonPath().getMap("[0]");

        assertAll(
                () -> assertThat(payment.get("id")).isEqualTo("P1"),
                () -> assertThat(payment.get("title")).isEqualTo("Product 1"),
                () -> assertThat(payment.get("description")).isEqualTo("Description 1"),
                () -> assertThat(payment.get("price")).isEqualTo(123.4f));
    }

    @Test
    @Sql(statements = {
            "INSERT INTO categories VALUES ('C2', 'uri-2', 'Category 2')",
            "INSERT INTO products VALUES ('P2', 'Product 2', 'Description 2', 123.4)",
            "INSERT INTO products_in_categories VALUES ('P2', 'C2')"
    })
    void products_by_category_are_listed() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/catalog/products")
                .get("uri-2")
                .andReturn()
                .jsonPath().getMap("[0]");

        assertAll(
                () -> assertThat(payment.get("id")).isEqualTo("P2"),
                () -> assertThat(payment.get("title")).isEqualTo("Product 2"),
                () -> assertThat(payment.get("description")).isEqualTo("Description 2"),
                () -> assertThat(payment.get("price")).isEqualTo(123.4f));
    }
}

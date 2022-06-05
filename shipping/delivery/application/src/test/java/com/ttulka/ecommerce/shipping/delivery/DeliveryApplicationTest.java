package com.ttulka.ecommerce.shipping.delivery;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    @Sql(statements = "INSERT INTO deliveries VALUES ('D001', 'TEST001', 'Test PersonA', 'Test Place 1');")
    void deliveries_are_listed() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/delivery")
                .get()
                .andReturn()
                .jsonPath().getMap("[0]");

        assertAll(
                () -> assertThat(payment.get("id")).isEqualTo("D001"),
                () -> assertThat(payment.get("orderId")).isEqualTo("TEST001"));
    }

    @Test
    @Sql(statements = "INSERT INTO deliveries VALUES ('D002', 'TEST002', 'Test PersonB', 'Test Place 2');")
    void delivery_for_an_order_is_returned() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/delivery/order")
                .get("TEST002")
                .andReturn()
                .jsonPath().get();

        assertThat(payment.get("id")).isEqualTo("D002");
    }
}

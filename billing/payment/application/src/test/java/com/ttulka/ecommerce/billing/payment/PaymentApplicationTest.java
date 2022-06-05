package com.ttulka.ecommerce.billing.payment;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = "INSERT INTO payments VALUES ('000', 'REF01', 123.4, 'NEW')")
class PaymentApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void payments_are_listed() {
        Map<String, Object> payment = with().log().ifValidationFails()
                .port(port)
                .basePath("/payment")
                .get()
                .andReturn()
                .jsonPath().getMap("[0]");

        assertAll(
                () -> assertThat(payment.get("id")).isEqualTo("000"),
                () -> assertThat(payment.get("referenceId")).isEqualTo("REF01"),
                () -> assertThat(payment.get("collected")).isEqualTo(false),
                () -> assertThat(payment.get("total")).isEqualTo(123.4f));
    }
}

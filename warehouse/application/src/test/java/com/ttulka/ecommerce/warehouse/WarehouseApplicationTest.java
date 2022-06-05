package com.ttulka.ecommerce.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = "INSERT INTO products_in_stock VALUES ('P001', 123)")
class WarehouseApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void stock_amount_is_returned() {
        Integer amount = with().log().ifValidationFails()
                .port(port)
                .basePath("/warehouse/stock")
                .get("P001")
                .as(Integer.class);

        assertThat(amount).isEqualTo(123);
    }
}

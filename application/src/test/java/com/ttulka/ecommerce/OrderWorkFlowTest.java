package com.ttulka.ecommerce;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data-order-workflow.sql")
class OrderWorkFlowTest extends OrderWorkFlow {

    @LocalServerPort
    private int port;

    @Override
    int port() {
        return port;
    }
}
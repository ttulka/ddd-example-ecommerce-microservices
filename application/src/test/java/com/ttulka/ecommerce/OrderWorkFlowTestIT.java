package com.ttulka.ecommerce;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class OrderWorkFlowTestIT extends OrderWorkFlow {

    @Value("${test.server.port:8080}")
    private int port;

    @Override
    int port() {
        return port;
    }
}
package com.ttulka.ecommerce.billing.payment.listeners;

import com.ttulka.ecommerce.billing.payment.CollectPayment;
import com.ttulka.ecommerce.billing.payment.ReferenceId;
import com.ttulka.ecommerce.common.primitives.Money;
import com.ttulka.ecommerce.sales.order.OrderPlaced;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Payment listener for OrderPlaced event.
 */
@Component("payment-orderPlacedListener") // a custom name to avoid collision
@RequiredArgsConstructor
class OrderPlacedListener {

    private final @NonNull CollectPayment collectPayment;

    @TransactionalEventListener
    @Async
    public void on(OrderPlaced event) {
        collectPayment.collect(
                new ReferenceId(event.orderId),
                new Money(event.total));
    }
}

package com.ttulka.ecommerce.shipping.dispatching;

import java.time.Instant;

import com.ttulka.ecommerce.common.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Delivery Dispatched domain event.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "orderId")
@ToString
public final class DeliveryDispatched implements DomainEvent {

    public Instant when;
    public String orderId;
}

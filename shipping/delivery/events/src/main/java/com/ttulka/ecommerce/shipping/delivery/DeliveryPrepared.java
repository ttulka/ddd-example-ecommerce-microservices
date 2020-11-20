package com.ttulka.ecommerce.shipping.delivery;

import java.time.Instant;

import com.ttulka.ecommerce.common.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Delivery Prepared domain event.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "orderId")
@ToString
public final class DeliveryPrepared implements DomainEvent {

    public Instant when;
    public String orderId;
}

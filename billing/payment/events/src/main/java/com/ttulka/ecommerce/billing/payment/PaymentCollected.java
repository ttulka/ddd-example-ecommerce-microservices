package com.ttulka.ecommerce.billing.payment;

import java.time.Instant;

import com.ttulka.ecommerce.common.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Payment Collected domain event.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "referenceId")
@ToString
public final class PaymentCollected implements DomainEvent {

    public Instant when;
    public String referenceId;
}

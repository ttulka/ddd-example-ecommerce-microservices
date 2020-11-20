package com.ttulka.ecommerce.warehouse;

import java.time.Instant;

import com.ttulka.ecommerce.common.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Goods Missed domain event.
 * <p>
 * Raised when a product is missed in the stock (sold out) and requested to be fetched.
 * <p>
 * Some other service could take care of it (eg. notify a supplier).
 * <br>In the current workflow the delivery is dispatched even when something is missing. This will be just delivered later.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"productCode", "amount"})
@ToString
public final class GoodsMissed implements DomainEvent {

    public Instant when;
    public String productCode;
    public Integer amount;
}

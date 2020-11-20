package com.ttulka.ecommerce.sales.order;

import java.time.Instant;
import java.util.Map;

import com.ttulka.ecommerce.common.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Order Placed domain event.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "orderId")
@ToString
public final class OrderPlaced implements DomainEvent {

    public Instant when;
    public String orderId;
    public Map<String, Integer> items;
    public Float total;
}

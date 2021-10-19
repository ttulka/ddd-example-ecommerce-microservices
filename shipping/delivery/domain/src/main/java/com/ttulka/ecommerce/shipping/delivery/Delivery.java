package com.ttulka.ecommerce.shipping.delivery;

/**
 * Delivery entity.
 */
public interface Delivery {

    DeliveryId id();

    OrderId orderId();

    Address address();

    /**
     * {@link DeliveryAlreadyPreparedException} is thrown when already prepared.
     */
    void prepare();

    /**
     * {@link DeliveryAlreadyDispatchedException} is thrown when already dispatched.
     */
    void dispatch();

    boolean isDispatched();

    final class DeliveryAlreadyPreparedException extends IllegalStateException {
    }

    final class DeliveryAlreadyDispatchedException extends IllegalStateException {
    }
}

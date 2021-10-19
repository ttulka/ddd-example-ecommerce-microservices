package com.ttulka.ecommerce.shipping.delivery;

/**
 * Prepare Delivery use-case.
 */
public interface PrepareDelivery {

    /**
     * Prepares a new delivery.
     *
     * {@link Delivery.DeliveryAlreadyPreparedException} is thrown when already prepared.
     *
     * @param orderId the order ID
     * @param address the delivery address
     */
    void prepare(OrderId orderId, Address address);
}

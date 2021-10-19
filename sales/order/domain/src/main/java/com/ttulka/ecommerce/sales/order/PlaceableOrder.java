package com.ttulka.ecommerce.sales.order;

/**
 * Placeable Order entity.
 */
public interface PlaceableOrder extends Order {

    /**
     * {@link OrderAlreadyPlacedException} is thrown when the order has already been placed
     */
    void place();

    /**
     * {@link OrderAlreadyPlacedException} is thrown when an already placed Order is placed.
     */
    final class OrderAlreadyPlacedException extends IllegalStateException {
    }
}

package com.ttulka.ecommerce.billing.payment;

import com.ttulka.ecommerce.common.primitives.Money;

/**
 * Payment entity.
 */
public interface Payment {

    PaymentId id();

    ReferenceId referenceId();

    Money total();

    /**
     * {@link PaymentAlreadyRequestedException} is thrown when the payment has already been requested
     */
    void request();

    /**
     * {@link PaymentNotRequestedYetException} is thrown when the payment has not been requested yet
     * {@link PaymentAlreadyCollectedException} is thrown when the payment has already collected
     */
    void collect();

    boolean isRequested();

    boolean isCollected();

    /**
     * {@link PaymentAlreadyRequestedException} is thrown when an already requested Payment is requested.
     */
    final class PaymentAlreadyRequestedException extends IllegalStateException {
    }

    /**
     * {@link PaymentNotRequestedYetException} is thrown when a Payment is collected but not requested yet.
     */
    final class PaymentNotRequestedYetException extends IllegalStateException {
    }

    /**
     * {@link PaymentAlreadyCollectedException} is thrown when an already collected Payment is collected.
     */
    final class PaymentAlreadyCollectedException extends IllegalStateException {
    }
}

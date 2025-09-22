package com.cabapp.model;

/**
 * Payment method options for the cab booking system
 */
public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    DIGITAL_WALLET("Digital Wallet"),
    PAYPAL("PayPal"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
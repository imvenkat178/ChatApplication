package com.cabapp.model.dto;

import com.cabapp.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentMethodId;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal tip;

    private String promoCode;

    private Boolean savePaymentMethod;

    private String stripeToken;

    private String cvv;
}
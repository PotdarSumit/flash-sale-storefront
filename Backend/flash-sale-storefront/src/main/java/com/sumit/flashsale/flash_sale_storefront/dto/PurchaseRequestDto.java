package com.sumit.flashsale.flash_sale_storefront.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
public record PurchaseRequestDto(
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity,
        @NotBlank String customerIdentifier,
        @NotNull @Positive BigDecimal displayedPrice
) {

}

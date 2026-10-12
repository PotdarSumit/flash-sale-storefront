package com.sumit.flashsale.flash_sale_storefront.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}


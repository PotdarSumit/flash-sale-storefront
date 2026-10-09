package com.sumit.flashsale.flash_sale_storefront.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}

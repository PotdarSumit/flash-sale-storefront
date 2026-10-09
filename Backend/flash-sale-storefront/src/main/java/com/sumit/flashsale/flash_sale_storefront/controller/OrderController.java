package com.sumit.flashsale.flash_sale_storefront.controller;

import com.sumit.flashsale.flash_sale_storefront.dto.OrderResponseDto;
import com.sumit.flashsale.flash_sale_storefront.dto.PurchaseRequestDto;
import com.sumit.flashsale.flash_sale_storefront.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(
            @Valid @RequestBody PurchaseRequestDto request
    ) {
        OrderResponseDto response = orderService.placeOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}



package com.sumit.flashsale.flash_sale_storefront.controller;

import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseInventoryDto;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/warehouse")
@RequiredArgsConstructor
public class WarehouseInventoryController {

    private final ProductRepository productRepository;

    @GetMapping("/inventory")
    public ResponseEntity<Page<WarehouseInventoryDto>> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        if (page < 0 || size < 1 || size > 500) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                productRepository.findWarehouseInventory(
                        PageRequest.of(page, size)
                )
        );
    }
}


package com.sumit.flashsale.flash_sale_storefront.controller;

import com.sumit.flashsale.flash_sale_storefront.dto.ProductResponseDto;
import com.sumit.flashsale.flash_sale_storefront.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products?page=0&size=20
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100")
            int size
    ) {
        return ResponseEntity.ok(
                productService.getAllProducts(page, size)
        );
    }

    @GetMapping("/products/flash-sale")
    public ResponseEntity<List<ProductResponseDto>> getFlashSaleProducts() {
        return ResponseEntity.ok(productService.getFlashSaleProducts());
    }
    // GET /api/products/1
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable
            @Min(value = 1, message = "Product ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    // GET /api/products/search?keyword=milk&page=0&size=20
    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam
            @NotBlank(message = "Keyword must not be blank")
            String keyword,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword, page, size)
        );
    }

    // GET /api/categories
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    // GET /api/products/category/Dairy?page=0&size=20
    @GetMapping("/products/category/{category}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable
            @NotBlank(message = "Category must not be blank")
            String category,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {
        return ResponseEntity.ok(
                productService.getProductsByCategory(category, page, size)
        );
    }
}

package com.sumit.flashsale.warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseInventory {

    private Long productId;
    private String sku;
    private int physicalStock;
    private BigDecimal price;
    private boolean flashSale;
}

package com.sumit.flashsale.flash_sale_storefront.seeder;

import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductSeeder implements CommandLineRunner {
    private static final Logger log =
            LoggerFactory.getLogger(ProductSeeder.class);

    private static final int TOTAL_PRODUCTS = 50_000;
    private static final int BATCH_SIZE = 1_000;
    private static final int LOG_INTERVAL = 5_000;

    private static final List<String> CATEGORIES = List.of(
            "Dairy",
            "Fruits",
            "Vegetables",
            "Beverages",
            "Snacks",
            "Grocery"
    );

    private static final List<String> PRODUCT_NAMES = List.of(
            "Milk", "Cheese", "Butter", "Yogurt", "Paneer",
            "Apple", "Banana", "Orange", "Mango", "Grapes",
            "Potato", "Tomato", "Onion", "Carrot", "Spinach",
            "Coffee", "Tea", "Juice", "Mineral Water", "Soft Drink",
            "Chips", "Biscuits", "Chocolate", "Cookies", "Popcorn",
            "Rice", "Wheat Flour", "Sugar", "Lentils", "Cooking Oil"
    );

    private static final List<String> BRANDS = List.of(
            "FreshFarm", "DailyChoice", "NatureBasket",
            "GoldenHarvest", "PureLiving", "FarmFresh",
            "GreenValley", "UrbanPantry", "HappyFoods",
            "PrimeSelect"
    );

    private final ProductRepository productRepository;
    private final Faker faker = new Faker(Locale.ENGLISH);

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Products already exist. Skipping product seeding.");
            return;
        }

        log.info("Starting product seeding: {} products", TOTAL_PRODUCTS);

        seedProducts();

        log.info("Product seeding completed successfully.");
    }

    @Transactional
    public void seedProducts() {
        for (int start = 0; start < TOTAL_PRODUCTS; start += BATCH_SIZE) {
            int batchEnd = Math.min(start + BATCH_SIZE, TOTAL_PRODUCTS);
            List<Product> batch = new java.util.ArrayList<>(BATCH_SIZE);

            for (int i = start; i < batchEnd; i++) {
                batch.add(createProduct(i + 1));
            }

            productRepository.saveAll(batch);

            int inserted = batchEnd;

            if (inserted % LOG_INTERVAL == 0) {
                log.info("Products inserted: {}/{}",
                        inserted, TOTAL_PRODUCTS);
            }
        }
    }

    private Product createProduct(int sequence) {
        Product product = new Product();

        String category = CATEGORIES.get(
                ThreadLocalRandom.current().nextInt(CATEGORIES.size())
        );

        String baseName = PRODUCT_NAMES.get(
                ThreadLocalRandom.current().nextInt(PRODUCT_NAMES.size())
        );

        String brand = BRANDS.get(
                ThreadLocalRandom.current().nextInt(BRANDS.size())
        );

        product.setSku(String.format("FS-%06d", sequence));
        product.setName(brand + " " + baseName + " " + faker.number().digits(3));
        product.setCategory(category);

        BigDecimal price = BigDecimal.valueOf(
                ThreadLocalRandom.current().nextDouble(10.0, 5000.0)
        ).setScale(2, RoundingMode.HALF_UP);


        product.setPrice(price);

        int initialStock = ThreadLocalRandom.current().nextInt(0, 501);
        product.setStock(initialStock);

// Initialize warehouse inventory fields
        product.setWarehouseStock(initialStock);
        product.setPendingWarehouseQuantity(0);

        product.setFlashSale(ThreadLocalRandom.current().nextInt(100) < 10);
        product.setUpdatedAt(LocalDateTime.now());


        return product;
    }
}

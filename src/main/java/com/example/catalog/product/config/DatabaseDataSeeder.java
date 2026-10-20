package com.example.catalog.product.config;

import com.example.catalog.product.model.Product;
import com.example.catalog.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DatabaseDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseDataSeeder.class);

    @Bean
    @Profile("!test")
    CommandLineRunner seedDatabase(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Database already contains data ({} records). Skipping seeding.", repository.count());
                return;
            }

            log.info("Seeding initial e-commerce product catalog data...");

            List<Product> seedProducts = List.of(
                // Electronics
                new Product("Apple MacBook Pro 16", "M3 Pro chip, 36GB Unified Memory, 512GB SSD Storage, Space Black", "electronics", new BigDecimal("2499.00"), "USD"),
                new Product("Apple iPhone 16 Pro", "Super Retina XDR display with ProMotion, Titanium finish, A18 Pro chip", "electronics", new BigDecimal("1099.00"), "USD"),
                new Product("Google Pixel 9 Pro", "Advanced Google Tensor G4 with Gemini Nano, Triple rear camera system", "electronics", new BigDecimal("999.00"), "USD"),
                new Product("Sony WH-1000XM5 Wireless Headphones", "Industry-leading noise cancellation, 30-hour battery life, Multipoint connection", "electronics", new BigDecimal("399.99"), "USD"),
                new Product("Dell UltraSharp 32 4K USB-C Hub Monitor", "IPS Black technology, HDR400, 99% DCI-P3 color coverage", "electronics", new BigDecimal("749.50"), "USD"),
                new Product("Logitech MX Master 3S Wireless Mouse", "Quiet clicks, 8K DPI tracking on glass, Ergonomic sculpted design", "electronics", new BigDecimal("99.99"), "USD"),
                new Product("Keychron Q1 Pro Wireless Custom Mechanical Keyboard", "75% QMK/VIA fully customizable aluminum body with red switches", "electronics", new BigDecimal("199.00"), "USD"),

                // Footwear
                new Product("Nike Air Zoom Pegasus 40", "Responsive road running shoes with breathable engineered mesh upper", "footwear", new BigDecimal("129.99"), "USD"),
                new Product("Adidas Ultraboost Light", "Epic energy return with Light BOOST midsole and Continental rubber outsole", "footwear", new BigDecimal("189.00"), "USD"),
                new Product("Hoka Clifton 9", "Ultra-cushioned everyday road running shoe with meta-rocker geometry", "footwear", new BigDecimal("145.00"), "USD"),
                new Product("Salomon Speedcross 6 Trail Running Shoes", "Aggressive grip for technical trails with Quicklace adjustment system", "footwear", new BigDecimal("140.00"), "USD"),
                new Product("Birkenstock Arizona Soft Footbed Leather Sandals", "Classic double-strap slide with anatomical cork footbed", "footwear", new BigDecimal("130.00"), "USD"),

                // Apparel
                new Product("Patagonia Nano Puff Jacket", "Windproof and water-resistant warm insulation jacket made with 100% recycled polyester", "apparel", new BigDecimal("239.00"), "USD"),
                new Product("Arc'teryx Beta LT Waterproof Jacket", "GORE-TEX shell offering lightweight, durable, windproof and waterproof protection", "apparel", new BigDecimal("450.00"), "USD"),
                new Product("Lululemon ABC Classic-Fit Pant", "Utilizes stretchy Warpstreme fabric with ergonomic gusset for all-day comfort", "apparel", new BigDecimal("128.00"), "USD"),
                new Product("Uniqlo Supima Cotton Crew Neck T-Shirt", "100% premium long-staple Supima cotton with a clean, smooth silhouette", "apparel", new BigDecimal("19.90"), "USD"),

                // Home & Kitchen
                new Product("Breville Barista Touch Espresso Machine", "Automated touchscreen espresso machine with integrated precision grinder", "kitchen", new BigDecimal("999.95"), "USD"),
                new Product("Fellow Stagg EKG Electric Pour-Over Kettle", "Precision pour spout with variable temperature control and LCD screen", "kitchen", new BigDecimal("165.00"), "USD"),
                new Product("Le Creuset Enameled Cast Iron Dutch Oven 5.5 Qt", "Exceptional heat distribution and retention with colorful porcelain enamel", "kitchen", new BigDecimal("420.00"), "USD"),
                new Product("Moccamaster KBGV Select Coffee Brewer", "Handmade drip coffee maker certified by SCAA with copper boiling element", "kitchen", new BigDecimal("359.00"), "USD"),

                // Fitness & Outdoors
                new Product("Garmin Forerunner 965 GPS Smartwatch", "Brilliant AMOLED display with premium titanium bezel and advanced training metrics", "outdoors", new BigDecimal("599.99"), "USD"),
                new Product("Osprey Farpoint 40 Travel Backpack", "Carry-on compliant ventilated backpanel travel pack with stowable harness", "outdoors", new BigDecimal("185.00"), "USD"),
                new Product("Yeti Tundra 45 Hard Cooler", "Rotomolded construction with PermaFrost insulation for rugged adventures", "outdoors", new BigDecimal("325.00"), "USD"),
                new Product("Black Diamond Trail Pro Trekking Poles", "FlickLock Pro adjustability with dual-density grip and padded straps", "outdoors", new BigDecimal("159.95"), "USD")
            );

            repository.saveAll(seedProducts);
            log.info("Successfully seeded {} realistic products into the catalog.", seedProducts.size());
        };
    }
}

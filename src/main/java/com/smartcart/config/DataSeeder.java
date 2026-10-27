package com.smartcart.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartcart.entity.Product;
import com.smartcart.repository.ProductRepository;

@Component
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final ProductRepository productRepository;

	public DataSeeder(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public void run(String... args) {
		seedSampleProducts();
	}

	public int seedSampleProducts() {
		List<Product> defaultProducts = List.of(
				new Product("Wireless Earbuds", "Electronics", 1499.0, "Bluetooth Wireless earbuds"),
				new Product("Mechanical Keyboard", "Electronics", 2499.0, "RGB mechanical gaming keyboard with blue switches"),
				new Product("Coffee Mug", "Home & Kitchen", 299.0, "Ceramic heat-insulated coffee mug"),
				new Product("Running Shoes", "Footwear", 1899.0, "Lightweight breathable running shoes"),
				new Product("Gaming Mouse", "Electronics", 899.0, "Ergonomic RGB gaming mouse with optical sensor"),
				new Product("Stainless Steel Water Bottle", "Home & Kitchen", 499.0, "Vacuum-insulated thermal water bottle 750ml"),
				new Product("Smart Fitness Watch", "Electronics", 3499.0, "AMOLED smartwatch with heart rate monitor and sleep tracking"),
				new Product("Trail Hiking Shoes", "Footwear", 2299.0, "Waterproof all-weather outdoor hiking shoes")
		);

		int insertedCount = 0;
		for (Product product : defaultProducts) {
			if (!productRepository.existsByNameIgnoreCase(product.getName())) {
				productRepository.save(product);
				insertedCount++;
				log.info("Seeded initial product: '{}' [Category: {}, Price: {}]",
						product.getName(), product.getCategory(), product.getPrice());
			}
		}

		if (insertedCount > 0) {
			log.info("Catalog initialization complete: {} new product(s) added.", insertedCount);
		} else {
			log.info("Catalog initialization check: all sample products already present in database.");
		}

		return insertedCount;
	}

}

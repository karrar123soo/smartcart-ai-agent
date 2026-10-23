package com.smartcart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartcart.entity.Product;
import com.smartcart.repository.ProductRepository;

@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	public List<Product> searchProductsByKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return productRepository.findAll();
		}
		return productRepository.searchByKeyword(keyword.trim());
	}

	public List<Product> getProductsByMaxPrice(double maxPrice) {
		return productRepository.findByPriceLessThanEqual(maxPrice);
	}

}

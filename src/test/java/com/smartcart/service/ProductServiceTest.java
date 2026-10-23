package com.smartcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartcart.entity.Product;
import com.smartcart.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository);
	}

	@Test
	void searchProductsByKeyword_withValidKeyword_shouldCallRepository() {
		Product p = new Product("Smart Watch", "Wearables", 199.99, "Fitness tracking smart watch");
		when(productRepository.searchByKeyword("watch")).thenReturn(List.of(p));

		List<Product> result = productService.searchProductsByKeyword("watch");

		assertThat(result).containsExactly(p);
		verify(productRepository).searchByKeyword("watch");
	}

	@Test
	void searchProductsByKeyword_withNullOrBlank_shouldReturnAllProducts() {
		Product p = new Product("Smart Watch", "Wearables", 199.99, "Fitness tracking smart watch");
		when(productRepository.findAll()).thenReturn(List.of(p));

		List<Product> nullResult = productService.searchProductsByKeyword(null);
		assertThat(nullResult).containsExactly(p);

		List<Product> blankResult = productService.searchProductsByKeyword("   ");
		assertThat(blankResult).containsExactly(p);
	}

	@Test
	void getProductsByMaxPrice_shouldCallRepository() {
		Product p = new Product("Earbuds", "Audio", 49.99, "Noise canceling earbuds");
		when(productRepository.findByPriceLessThanEqual(100.0)).thenReturn(List.of(p));

		List<Product> result = productService.getProductsByMaxPrice(100.0);

		assertThat(result).containsExactly(p);
		verify(productRepository).findByPriceLessThanEqual(100.0);
	}

}

package com.smartcart.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartcart.entity.Product;
import com.smartcart.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

	@Mock
	private ProductRepository productRepository;

	private DataSeeder dataSeeder;

	@BeforeEach
	void setUp() {
		dataSeeder = new DataSeeder(productRepository);
	}

	@Test
	void seedSampleProducts_whenDatabaseIsEmpty_shouldInsertAllSampleProducts() {
		when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);

		int inserted = dataSeeder.seedSampleProducts();

		assertThat(inserted).isEqualTo(8);
		verify(productRepository, times(8)).save(any(Product.class));
	}

	@Test
	void seedSampleProducts_whenProductsAlreadyExist_shouldNotInsertDuplicates() {
		when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

		int inserted = dataSeeder.seedSampleProducts();

		assertThat(inserted).isZero();
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	void seedSampleProducts_whenSomeExist_shouldOnlyInsertMissingProducts() {
		when(productRepository.existsByNameIgnoreCase("Wireless Earbuds")).thenReturn(true);
		when(productRepository.existsByNameIgnoreCase("Mechanical Keyboard")).thenReturn(true);
		when(productRepository.existsByNameIgnoreCase("Coffee Mug")).thenReturn(true);
		when(productRepository.existsByNameIgnoreCase("Running Shoes")).thenReturn(true);
		// Remaining 4 do not exist
		when(productRepository.existsByNameIgnoreCase("Gaming Mouse")).thenReturn(false);
		when(productRepository.existsByNameIgnoreCase("Stainless Steel Water Bottle")).thenReturn(false);
		when(productRepository.existsByNameIgnoreCase("Smart Fitness Watch")).thenReturn(false);
		when(productRepository.existsByNameIgnoreCase("Trail Hiking Shoes")).thenReturn(false);

		int inserted = dataSeeder.seedSampleProducts();

		assertThat(inserted).isEqualTo(4);
		verify(productRepository, times(4)).save(any(Product.class));
	}

}

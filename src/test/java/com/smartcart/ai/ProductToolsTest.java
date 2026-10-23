package com.smartcart.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import com.smartcart.entity.Product;
import com.smartcart.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductToolsTest {

	@Mock
	private ProductService productService;

	private ProductTools productTools;

	@BeforeEach
	void setUp() {
		productTools = new ProductTools(productService);
	}

	@Test
	void searchProducts_shouldDelegateToProductService() {
		Product p = new Product("Wireless Earbuds", "Electronics", 1499.0, "Bluetooth Wireless earbuds");
		p.setId(1L);

		when(productService.searchProductsByKeyword("earbuds")).thenReturn(List.of(p));

		List<Product> result = productTools.searchProducts("earbuds");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Wireless Earbuds");
		assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
		assertThat(result.get(0).getPrice()).isEqualTo(1499.0);
		assertThat(result.get(0).getDescription()).isEqualTo("Bluetooth Wireless earbuds");

		verify(productService).searchProductsByKeyword("earbuds");
	}

	@Test
	void searchProducts_shouldHandleNoMatchesSafely() {
		when(productService.searchProductsByKeyword("nonexistent")).thenReturn(List.of());

		List<Product> result = productTools.searchProducts("nonexistent");

		assertThat(result).isNotNull().isEmpty();
		verify(productService).searchProductsByKeyword("nonexistent");
	}

	@Test
	void searchProducts_shouldHandleDatabaseErrorSafely() {
		when(productService.searchProductsByKeyword("error")).thenThrow(new DataAccessException("DB failure") {});

		List<Product> result = productTools.searchProducts("error");

		assertThat(result).isNotNull().isEmpty();
		verify(productService).searchProductsByKeyword("error");
	}

	@Test
	void getProductsByMaxPrice_shouldDelegateToProductService() {
		Product p = new Product("USB-C Cable", "Accessories", 12.50, "Fast charging cable");
		p.setId(2L);

		when(productService.getProductsByMaxPrice(50.0)).thenReturn(List.of(p));

		List<Product> result = productTools.getProductsByMaxPrice(50.0);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("USB-C Cable");
		assertThat(result.get(0).getPrice()).isEqualTo(12.50);

		verify(productService).getProductsByMaxPrice(50.0);
	}

	@Test
	void getProductsByMaxPrice_shouldHandleDatabaseErrorSafely() {
		when(productService.getProductsByMaxPrice(100.0)).thenThrow(new DataAccessException("DB failure") {});

		List<Product> result = productTools.getProductsByMaxPrice(100.0);

		assertThat(result).isNotNull().isEmpty();
		verify(productService).getProductsByMaxPrice(100.0);
	}

}

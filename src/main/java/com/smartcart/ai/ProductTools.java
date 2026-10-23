package com.smartcart.ai;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.smartcart.entity.Product;
import com.smartcart.service.ProductService;

@Component
public class ProductTools {

	private static final Logger log = LoggerFactory.getLogger(ProductTools.class);

	private final ProductService productService;

	public ProductTools(ProductService productService) {
		this.productService = productService;
	}

	@Tool(name = "searchProducts", description = "Search existing products in the store by keyword matching name, category, or description. Returns relevant product details including name, category, price, and description.")
	public List<Product> searchProducts(
			@ToolParam(description = "Keyword to search for in product name, category, or description", required = false) String keyword) {
		try {
			List<Product> products = productService.searchProductsByKeyword(keyword);
			return (products != null) ? products : Collections.emptyList();
		} catch (Exception e) {
			log.error("Database query error while searching products by keyword '{}': {}", keyword, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Tool(name = "getProductsByMaxPrice", description = "Retrieve existing products in the store priced at or below a specified maximum price. Returns matching products including name, category, price, and description.")
	public List<Product> getProductsByMaxPrice(
			@ToolParam(description = "Maximum price or budget threshold", required = false) Double maxPrice) {
		try {
			if (maxPrice == null || maxPrice < 0) {
				return Collections.emptyList();
			}
			List<Product> products = productService.getProductsByMaxPrice(maxPrice);
			return (products != null) ? products : Collections.emptyList();
		} catch (Exception e) {
			log.error("Database query error while retrieving products below maxPrice '{}': {}", maxPrice, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

}

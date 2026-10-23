package com.smartcart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcart.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT p FROM Product p WHERE " +
	       "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	       "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	       "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Product> searchByKeyword(@Param("keyword") String keyword);

	List<Product> findByPriceLessThanEqual(double maxPrice);

}

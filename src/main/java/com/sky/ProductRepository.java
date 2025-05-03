package com.sky;


import io.quarkus.hibernate.reactive.panache.PanacheRepository;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;


@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

	public Uni<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
		return find("price >= ?1 and price <= ?2", minPrice, maxPrice)
				.page(page, size)
				.list();
	}

	public Uni<Long> countByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
		return count("price >= ?1 and price <= ?2", minPrice, maxPrice);
	}

	public Uni<Product> findByIdReactive(Long id) {
		return findById(id)
				.onItem().ifNull().failWith(() -> new IllegalArgumentException("Product not found"));
	}

	@Transactional
	public Uni<Product> addProduct(Product product) {
		return persistAndFlush(product);
	}

	@Transactional
	public Uni<Boolean> deleteProduct(Long id) {
		return deleteById(id);
	}


	public Uni<List<Product>> findByNameContaining(String name) {
		String searchTerm = "%" + name.toLowerCase() + "%";
		return find("lower(name) like ?1", searchTerm)
				.list();
	}

	public Uni<List<Product>> findLowStockProducts(int threshold, int page, int size) {
		return find("stock < ?1", threshold)
				.page( page, size)
				.list();

	}

}

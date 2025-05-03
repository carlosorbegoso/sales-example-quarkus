package com.sky;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@ApplicationScoped
public class ProductService {
	@Inject
	ProductRepository productRepository;

	@WithSession
	public Uni<PaginatedResponse<Product>> findAll(int page, int size) {
		return productRepository.findAll()
				.page(page, size)
				.list()
				.chain(products -> productRepository.count()
						.map(totalElements -> new PaginatedResponse<>(
								products,
								page,
								size,
								totalElements,
								(int) Math.ceil((double) totalElements / size)
						))
				);

	}

	@WithSession
	public Uni<Product> findById(Long id) {
		return productRepository.findByIdReactive(id);
	}

	@WithSession
	public Uni<PaginatedResponse<Product>> findByNameContaining(String name) {
		return productRepository.findByNameContaining(name)
				.chain(products -> productRepository.count()
						.map(totalElements -> new PaginatedResponse<>(
								products,
								0,
								products.size(),
								totalElements,
								(int) Math.ceil((double) totalElements / products.size())
						))
				);
	}

	@WithTransaction
	public Uni<Product> saveProduct(Product product) {
		return Uni.createFrom().item(() -> validateProduct(product))
				.chain(productRepository::addProduct);
	}

	@WithTransaction
	public Uni<Product> updateProduct(Long id, Product updateProduct){
		return productRepository.findByIdReactive(id)
				.onItem().ifNotNull().transformToUni(
						existingProduct ->{
					updateProduct.setId(id);
					validateProduct(updateProduct);
					return productRepository.addProduct(updateProduct);
				}).onItem().ifNull().failWith(() -> new IllegalArgumentException("Product not found"));
	}
	@WithTransaction
	public Uni<Boolean> deleteProduct(Long id) {
		return productRepository.deleteProduct(id);
	}


	public Uni<PaginatedResponse<Product>> getLowStockProducts(int threshold, int page, int size) {
		return productRepository.findLowStockProducts(threshold, page, size)
				.chain(products -> productRepository.find( "stock < ?1", threshold)
						.count()
						.map(totalElements -> new PaginatedResponse<>(
								products,
								page,
								size,
								totalElements,
								(int) Math.ceil((double) totalElements / size)
						))
				);
	}
	// sales product

	@WithTransaction
	public Uni<Boolean> purchaseProduct(Long id, int quantity){
		return productRepository.findByIdReactive(id)
				.onItem().ifNotNull().transformToUni(product -> {
					if (product.reduceStock(quantity)){
						return productRepository.addProduct(product)
								.map(update -> true);
					} else {
						return Uni.createFrom().failure(
								new IllegalArgumentException("Not enough stock available"));
					}
				}).onItem().ifNull().failWith(() -> new IllegalArgumentException("Product not found"));
	}


	public Uni<BigDecimal> calculateTotalInventoryValue(){
		return productRepository.listAll()
				.onItem().transform(products -> products.stream()
						.map(product -> product.getPrice().multiply(new BigDecimal(product.getStock())))
						.reduce(BigDecimal.ZERO, BigDecimal::add));
	}


	private Product validateProduct(Product product){
		if(product.getName() == null || product.getName().trim().isEmpty()){
			throw new IllegalArgumentException("Product name cannot be null or empty");
		}
		if(product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0){
			throw new IllegalArgumentException("Product price must be greater than zero");
		}
		if(product.getStock() < 0){
			throw new IllegalArgumentException("Product stock cannot be negative");
		}

		return product;
	}

	@WithSession
	public Uni<PaginatedResponse<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size){
		return productRepository.findByPriceRange(minPrice, maxPrice, page, size)
				.chain(products -> productRepository.countByPriceRange(minPrice, maxPrice)
						.map(totalElements -> new PaginatedResponse<>(
								products,
								page,
								size,
								totalElements,
								(int) Math.ceil((double) totalElements / size)
						))
				);
	}

	public Multi<Product> applyDiscount(List<Long> productIds, BigDecimal discountPercentage) {
		return Multi.createFrom().iterable(productIds)
				.onItem().transformToUniAndConcatenate(id ->
						productRepository.findByIdReactive(id)
								.onItem().ifNotNull().transformToUni(product -> {
									BigDecimal discount = product.getPrice().multiply(discountPercentage)
											.divide(new BigDecimal(100));
									product.setPrice(product.getPrice().subtract(discount));
									return productRepository.addProduct(product);
								})
				)
				.filter(Objects::nonNull);
	}
}

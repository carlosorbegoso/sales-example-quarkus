package com.sky;

import jakarta.persistence.*;


import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "description")
	private String description;
	@Column(name = "price", nullable = false)
	private BigDecimal price;
	@Column(name = "stock", nullable = false)
	private int stock;

	public Product() {
	}

	public Product(Long id, String name, String description, BigDecimal price, int stock) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.stock = stock;
	}
	// Getters and Setters encapsulated
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}

	public boolean reduceStock(int quantity){
		if (stock >= quantity){
			stock -= quantity;
			return true;
		} else {
			return false;
		}
	}


}

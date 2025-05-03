package com.sky;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;


@Path("/products")
public class ResourceProduct {
	@Inject
	ProductService productService;

	@GET
	public Uni<PaginatedResponse<Product>> getAllProducts(
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("size") @DefaultValue("10") int size
	) {
		return productService.findAll( page, size);
	}

	@GET
	@Path("/{id}")
	public Uni<Response> getProductById(@PathParam("id") Long id){
		return productService.findById(id)
				.map(product -> {
					if(product == null){
						return Response.status(Response.Status.NOT_FOUND).build();
					}
					return Response.ok(product).build();
				});
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> createProduct( Product product){
		return productService.saveProduct(product)
				.map(create -> Response.status(Response.Status.CREATED).entity(create).build())
				.onFailure().recoverWithItem(failure -> Response.status(Response.Status.BAD_REQUEST)
						.entity(failure.getMessage()).build());
	}
	@PUT
	@Path("/{id}")
	public Uni<Response> updateProduct(@PathParam("id") Long id, Product product){
		return  productService.updateProduct(id, product)
				.map(updated -> Response.ok(updated).build())
				.onFailure().recoverWithItem(failure -> Response.status(Response.Status.BAD_REQUEST)
						.entity(failure.getMessage()).build());
	}
	@DELETE
	@Path("/{id}")
	public Uni<Response> deleteProduct(@PathParam("id") Long id){
		return productService.deleteProduct(id)
				.map(delete -> {
					if(delete){
						return Response.noContent().build();
					}
					return Response.status(Response.Status.NOT_FOUND).build();
				});
	}

	@GET
	@Path("/search")
	public Uni<PaginatedResponse<Product>> searchProducts(@QueryParam("name") String name){
		return productService.findByNameContaining(name);
	}

	@GET
	@Path("/low-stock")
	public Uni<PaginatedResponse<Product>> getLowStockProducts(@QueryParam("threshold") @DefaultValue("5")  int threshold,
	                                                           @QueryParam("page") @DefaultValue("0") int page,
	                                                           @QueryParam("size") @DefaultValue("10") int size){
		return productService.getLowStockProducts(threshold, page, size);
	}


	@POST
	@Path("/purchase/{id}")
	public Uni<Response> purchaseProduct(@PathParam("id") Long id, @QueryParam("quantity") int quantity){
		return productService.purchaseProduct(id, quantity)
				.map(success ->{
					if(success){
						return Response.ok().build();
					}
					return Response.status(Response.Status.BAD_REQUEST)
							.entity("Not enough stock available").build();
				})
				.onFailure().recoverWithItem(failure -> Response.status(Response.Status.BAD_REQUEST)
						.entity(failure.getMessage()).build());
	}

	@GET
	@Path("/inventory-value")
	public Uni<Response> getTotalInventoryValue(){
		return productService.calculateTotalInventoryValue()
				.onItem().transform(value ->
						Response.ok(value).build());

	}

	@GET
	@Path("/price-range")
	public Uni<PaginatedResponse<Product>> getProductsByPriceRange(@QueryParam("min") BigDecimal min,
	                                                               @QueryParam("max") BigDecimal max,
	                                                               @QueryParam("page") @DefaultValue("0") int page,
	                                                               @QueryParam("size") @DefaultValue("10") int size) {
		return productService.findByPriceRange(min, max, page, size);
	}


}


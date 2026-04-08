package com.batistell.catalogapi.controller;

import com.batistell.catalogapi.model.Product;
import com.batistell.catalogapi.model.ProductResponse;
import com.batistell.catalogapi.model.ErrorResponse;
import com.batistell.catalogapi.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Products", description = "Products API catalog operations")
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Add new products", description = "Add new products to the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products array to add",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductResponse> addProduct(
            @RequestBody @Valid List<Product> products) {
        
        String messageId = UUID.randomUUID().toString();
        log.info("messageId={} Starting AddProducts request", messageId);

        catalogService.addProducts(messageId, products);

        log.info("messageId={} AddProducts request completed successfully", messageId);
        return ResponseEntity.ok(new ProductResponse("Products added successfully", null));
    }

    @Operation(summary = "Get all products", description = "Retrieve all products from the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Product.class))))
    })
    @GetMapping(value = "/products", produces = "application/json")
    public ResponseEntity<List<Product>> getAllProducts() {
        String messageId = UUID.randomUUID().toString();
        log.info("messageId={} Starting GetAllProducts request", messageId);

        List<Product> products = catalogService.getAllProducts();

        log.info("messageId={} GetAllProducts request completed successfully", messageId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID", description = "Retrieve a product from the catalog by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/products/{id}", produces = "application/json")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable("id") String id) {
        
        String messageId = UUID.randomUUID().toString();
        log.info("messageId={} Starting GetProductByID request", messageId);

        Product product = catalogService.getProductById(id);

        log.info("messageId={} GetProductByID request completed successfully", messageId);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Update a product", description = "Update a product in the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product or ID supplied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/products/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable("id") String id,
            @RequestBody @Valid Product productToUpdate) {

        String messageId = UUID.randomUUID().toString();
        log.info("messageId={} Starting UpdateProduct request", messageId);

        Product updatedProduct = catalogService.updateProduct(id, productToUpdate);

        log.info("messageId={} UpdateProduct request completed successfully", messageId);
        return ResponseEntity.ok(new ProductResponse("Product updated successfully", updatedProduct));
    }

    @Operation(summary = "Delete a product", description = "Delete a product from the catalog by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable("id") String id) {

        String messageId = UUID.randomUUID().toString();
        log.info("messageId={} Starting DeleteProduct request", messageId);

        catalogService.deleteProduct(id);

        log.info("messageId={} DeleteProduct request completed successfully", messageId);
        return ResponseEntity.noContent().build();
    }
}

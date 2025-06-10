package org.example2.controllers;

import jakarta.validation.Valid;
import org.example2.payloads.DTOs.CategoryDTO;
import org.example2.payloads.DTOs.ProductDTO;
import org.example2.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/public/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }

    @PostMapping("/public/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@PathVariable Long categoryId, @RequestBody @Valid ProductDTO productDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.addProduct(categoryId, productDTO));
    }
}

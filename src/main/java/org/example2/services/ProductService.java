package org.example2.services;

import org.example2.payloads.DTOs.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO addProduct(Long categoryId, ProductDTO productDTO);
}

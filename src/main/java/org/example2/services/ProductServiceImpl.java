package org.example2.services;

import org.example2.models.Category;
import org.example2.models.Product;
import org.example2.payloads.DTOs.ProductDTO;
import org.example2.repositories.CategoryRepository;
import org.example2.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ModelMapper modelMapper, ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(category -> modelMapper
                        .map(category, ProductDTO.class))
                .toList();
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        if (optionalCategory.isPresent()) {
            product.setCategory(optionalCategory.get());
            optionalCategory.get().getProducts().add(product);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }

        throw new RuntimeException("Category with id " + categoryId + " not found");
    }
}

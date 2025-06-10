package org.example2.services;

import org.example2.payloads.DTOs.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    CategoryDTO createCategory(CategoryDTO categoryDTO);
}

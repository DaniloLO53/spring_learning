package org.example2.services;

import org.example2.payloads.DTOs.CategoryDTO;
import org.example2.payloads.responses.PageResponse;

import java.util.List;

public interface CategoryService {
    PageResponse<CategoryDTO> getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, Boolean ascending);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
}

package org.example2.services;

import org.example2.models.Category;
import org.example2.payloads.DTOs.CategoryDTO;
import org.example2.payloads.responses.PageResponse;
import org.example2.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PageResponse<CategoryDTO> getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, Boolean ascending) {
        Sort sortAndOrder = ascending
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortAndOrder);

        Page<Category> categoriesPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoriesPage.getContent();
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        PageResponse<CategoryDTO> categoryResponse = new PageResponse<>();

        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoriesPage.getNumber());
        categoryResponse.setPageSize(categoriesPage.getSize());
        categoryResponse.setTotalPages(categoriesPage.getTotalPages());
        categoryResponse.setTotalElements(categoriesPage.getTotalElements());
        categoryResponse.setLastPage(categoriesPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}

package org.example2.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.example2.config.AppConstants;
import org.example2.payloads.DTOs.CategoryDTO;
import org.example2.payloads.responses.PageResponse;
import org.example2.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryDTO));
    }

    @GetMapping("/public/categories")
    public ResponseEntity<PageResponse<CategoryDTO>> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.CategoryConstants.pageNumber, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.CategoryConstants.pageSize, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.CategoryConstants.sortBy, required = false)
            @Pattern(regexp = "id|name") String sortBy,
            @RequestParam(name = "ascending", defaultValue = AppConstants.CategoryConstants.ascending, required = false) Boolean ascending
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getAllCategories(pageNumber, pageSize, sortBy, ascending));
    }
}

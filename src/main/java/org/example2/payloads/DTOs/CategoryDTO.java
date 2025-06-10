package org.example2.payloads.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank
    @Size(min = 3, message = "Category name must have more than 3 characters")
    private String name;

    @JsonIgnore
    private Set<ProductDTO> products;
}

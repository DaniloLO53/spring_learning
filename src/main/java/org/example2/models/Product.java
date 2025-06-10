package org.example2.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50, message = "Title must have between 3 and 50 characters")
    private String title;

    @Size(max = 1000, message = "Description must have less than 1000 characters")
    private String description;

    @NotNull
    private Double price;

    @NotNull
    @Min(value = 1, message = "Quantity must be greater than 1")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

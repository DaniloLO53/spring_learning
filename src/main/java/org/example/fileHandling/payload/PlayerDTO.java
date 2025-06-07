package org.example.fileHandling.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private Long id;

    @NotNull
    private Boolean active;

    @NotNull
    private String name;

    @JsonIgnore
    private ProfileDTO profile;
}

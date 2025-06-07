package org.example.fileHandling.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "players")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Boolean active;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private Profile profile;
}

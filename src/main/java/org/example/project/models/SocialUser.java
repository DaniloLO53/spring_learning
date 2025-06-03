package org.example.project.models;

import jakarta.persistence.*;

@Entity
public class SocialUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "socialUser")
    private SocialProfile socialProfile;
}

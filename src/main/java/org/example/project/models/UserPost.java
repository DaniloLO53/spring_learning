package org.example.project.models;

import jakarta.persistence.*;

@Entity
public class UserPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private SocialUser socialUser;
}

package org.example.project.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SocialUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "socialUser")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "socialUser")
    private List<UserPost> userPosts = new ArrayList<>();
}

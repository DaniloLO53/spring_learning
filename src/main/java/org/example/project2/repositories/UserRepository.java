package org.example.project2.repositories;

import org.example.project2.models.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<SocialUser, Long> {
    SocialUser findByName(String name);
}

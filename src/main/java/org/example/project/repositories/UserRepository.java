package org.example.project.repositories;

import org.example.project.models.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<SocialUser, Long> {
}

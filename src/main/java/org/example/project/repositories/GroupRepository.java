package org.example.project.repositories;

import org.example.project.models.SocialGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<SocialGroup, Long> {
}

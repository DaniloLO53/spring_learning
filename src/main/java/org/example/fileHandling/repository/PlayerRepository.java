package org.example.fileHandling.repository;

import org.example.fileHandling.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}

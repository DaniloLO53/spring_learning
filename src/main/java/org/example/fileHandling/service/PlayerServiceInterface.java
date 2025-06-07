package org.example.fileHandling.service;

import org.example.fileHandling.payload.PlayerDTO;
import org.example.fileHandling.payload.ProfileDTO;

public interface PlayerServiceInterface {
    ProfileDTO createPlayer(PlayerDTO playerDTO);
}

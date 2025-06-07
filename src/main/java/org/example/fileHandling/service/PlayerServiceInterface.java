package org.example.fileHandling.service;

import org.example.fileHandling.payload.PlayerDTO;
import org.example.fileHandling.payload.ProfileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PlayerServiceInterface {
    ProfileDTO createPlayer(PlayerDTO playerDTO);
    ProfileDTO updateProfileImage(Long playerId, MultipartFile image) throws IOException;
}

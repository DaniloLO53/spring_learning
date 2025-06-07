package org.example.fileHandling.controller;

import org.example.fileHandling.payload.PlayerDTO;
import org.example.fileHandling.payload.ProfileDTO;
import org.example.fileHandling.service.FileService;
import org.example.fileHandling.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class PlayerController {
    private PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players")
    public ResponseEntity<ProfileDTO> createPlayer(@RequestBody PlayerDTO playerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(playerDTO));
    }

    @PutMapping("/players/{playerId}/image")
    public ResponseEntity<ProfileDTO> updateProfileImage(@PathVariable Long playerId, @RequestParam MultipartFile image) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.updateProfileImage(playerId, image));
    }
}

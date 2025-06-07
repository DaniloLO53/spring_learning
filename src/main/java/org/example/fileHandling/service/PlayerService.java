package org.example.fileHandling.service;

import org.example.fileHandling.model.Player;
import org.example.fileHandling.model.Profile;
import org.example.fileHandling.payload.PlayerDTO;
import org.example.fileHandling.payload.ProfileDTO;
import org.example.fileHandling.repository.PlayerRepository;
import org.example.fileHandling.repository.ProfileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;

@Service
public class PlayerService implements PlayerServiceInterface {
    private PlayerRepository playerRepository;
    private ProfileRepository profileRepository;
    private FileService fileService;
    private ModelMapper modelMapper;

    @Value("${fileHandling.image}")
    private String path;

    public PlayerService(PlayerRepository playerRepository, ProfileRepository profileRepository, FileService fileService, ModelMapper modelMapper) {
        this.playerRepository = playerRepository;
        this.profileRepository = profileRepository;
        this.fileService = fileService;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProfileDTO createPlayer(PlayerDTO playerDTO) {
        Player player = modelMapper.map(playerDTO, Player.class);

        Profile profile = new Profile();
        profile.setPlayer(player);
        profile.setName(playerDTO.getName());
        profile.setImage("default.jpg");
        player.setProfile(profile);

        profileRepository.save(profile);
        Player savedPlayer = playerRepository.save(player);
        Profile savedProfile = savedPlayer.getProfile();

        return modelMapper.map(savedProfile, ProfileDTO.class);
    }

    @Override
    public ProfileDTO updateProfileImage(Long playerId, MultipartFile image) throws IOException {
        Optional<Player> optionalExistingPlayer = playerRepository.findById(playerId);

        if (optionalExistingPlayer.isPresent()) {
            Player player = optionalExistingPlayer.get();
            Profile profile = player.getProfile();

            String fileName = fileService.uploadImage(path, image);
            profile.setImage(fileName);

            Profile updatedProfile = profileRepository.save(profile);
            playerRepository.save(player);

            return modelMapper.map(updatedProfile, ProfileDTO.class);
        }

        throw new RuntimeException("Player not found");
    }
}

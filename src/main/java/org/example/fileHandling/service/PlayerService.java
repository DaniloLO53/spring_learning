package org.example.fileHandling.service;

import org.example.fileHandling.model.Player;
import org.example.fileHandling.model.Profile;
import org.example.fileHandling.payload.PlayerDTO;
import org.example.fileHandling.payload.ProfileDTO;
import org.example.fileHandling.repository.PlayerRepository;
import org.example.fileHandling.repository.ProfileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PlayerService implements PlayerServiceInterface {
    private PlayerRepository playerRepository;
    private ProfileRepository profileRepository;
    private ModelMapper modelMapper;

    public PlayerService(PlayerRepository playerRepository, ProfileRepository profileRepository, ModelMapper modelMapper) {
        this.playerRepository = playerRepository;
        this.profileRepository = profileRepository;
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
}

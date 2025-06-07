package org.example.project.config;

import org.example.project.models.Post;
import org.example.project.models.Profile;
import org.example.project.models.SocialUser;
import org.example.project.models.SocialGroup;
import org.example.project.repositories.GroupRepository;
import org.example.project.repositories.PostRepository;
import org.example.project.repositories.ProfileRepository;
import org.example.project.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final ProfileRepository profileRepository;

    public DataInitializer(UserRepository userRepository, GroupRepository groupRepository, PostRepository postRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.groupRepository = groupRepository;
        this.profileRepository = profileRepository;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            SocialUser user1 = new SocialUser();
            user1.setName("Danilo");

            SocialUser user2 = new SocialUser();
            user2.setName("Cássia");
            userRepository.save(user2);

            SocialUser user3 = new SocialUser();
            user3.setName("Eliane");
            userRepository.save(user3);

            Profile user1Profile = new Profile();
            Profile user2Profile = new Profile();
            Profile user3Profile = new Profile();

            user1.setUserProfile(user1Profile);
            user1Profile.setSocialUser(user1);
            userRepository.save(user1);

            user2Profile.setSocialUser(user2);
            user2.setUserProfile(user2Profile);
            profileRepository.save(user2Profile);

            user3Profile.setSocialUser(user3);
            user3.setUserProfile(user3Profile);
            profileRepository.save(user3Profile);

            SocialGroup group1 = new SocialGroup();
            group1.getUsers().add(user1);
            group1.getUsers().add(user2);
            group1.getUsers().add(user3);
            user1.getGroups().add(group1);
            user2.getGroups().add(group1);
            user3.getGroups().add(group1);
            groupRepository.save(group1);

            Post post1 = new Post();
            post1.setAuthor(user1);
            postRepository.save(post1);

            Post post2 = new Post();
            post2.setAuthor(user2);
            postRepository.save(post2);
        };
    }
}

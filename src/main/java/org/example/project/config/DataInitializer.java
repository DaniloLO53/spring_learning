package org.example.project.config;

import org.example.project.models.Post;
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

    public DataInitializer(UserRepository userRepository, GroupRepository groupRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.groupRepository = groupRepository;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            SocialUser user1 = new SocialUser();
            user1.setName("Danilo");
            SocialUser user2 = new SocialUser();
            user2.setName("Cássia");
            SocialUser user3 = new SocialUser();
            user3.setName("Eliane");

            SocialGroup group1 = new SocialGroup();
            group1.getUsers().add(user1);
            group1.getUsers().add(user2);
            group1.getUsers().add(user3);
            user1.getGroups().add(group1);
            user2.getGroups().add(group1);
            user3.getGroups().add(group1);
            groupRepository.save(group1);

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);

            Post post1 = new Post();
            post1.setAuthor(user1);
            postRepository.save(post1);

            Post post2 = new Post();
            post2.setAuthor(user2);
            postRepository.save(post2);
        };
    }
}

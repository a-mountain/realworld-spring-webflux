package com.realworld.springmongo.user;

import helpers.user.UserSamples;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    PasswordService passwordService = new PasswordService();

    @Test
    void shouldReturnTrueWhenUserHasFollowee() {
        var user1 = createUser("username 1").build();
        var user2 = createUser("username 2").build();
        var user3 = createUser("username 3").followeeIds(List.of(user1.getId(), user2.getId())).build();
        userRepository.saveAll(List.of(user1, user2, user3)).blockLast();
        var exists = userRepository.existsByIdAndFolloweeIdsContains(user3.getId(), List.of(user1.getId())).block();
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNoFollowee() {
        var user1 = createUser("username 1").build();
        var user2 = createUser("username 2").build();
        var user3 = createUser("username 3").followeeIds(List.of(user1.getId(), user2.getId())).build();
        userRepository.saveAll(List.of(user1, user2, user3)).blockLast();
        var exists = userRepository.existsByIdAndFolloweeIdsContains(user1.getId(), List.of(user2.getId())).block();
        assertThat(exists).isFalse();
    }

    private User.UserBuilder createUser(String username) {
        return UserSamples.sampleUser(passwordService)
                .id(UUID.randomUUID().toString())
                .username(username);
    }
}
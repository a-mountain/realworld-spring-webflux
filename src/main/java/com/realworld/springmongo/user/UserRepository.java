package com.realworld.springmongo.user;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByIdAndFolloweeIdsContains(String id, List<String> followeeIds);

    Mono<User> findByUsername(String username);
}

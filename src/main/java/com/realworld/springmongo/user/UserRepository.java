package com.realworld.springmongo.user;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByUsername(String username);

    Mono<User> findByUsername(String username);

    default Mono<User> findAuthorByArticle(Article article) {
        return findById(article.getAuthorId());
    }

    default Mono<User> findByUsernameOrFail(String username) {
        return findByUsername(username)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Username", "not found")));
    }

    default Mono<User> findByEmailOrFail(String email) {
        return findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Email", "not found")));
    }
}

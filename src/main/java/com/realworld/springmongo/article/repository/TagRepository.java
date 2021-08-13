package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Tag;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.function.BiConsumer;

public interface TagRepository extends ReactiveMongoRepository<Tag, String> {

    default Flux<Tag> saveAllTags(Iterable<String> tags) {
        return Flux.fromIterable(tags)
                .flatMap(it -> save(Tag.of(it)))
                .onErrorContinue(DuplicateKeyException.class, nothing());
    }

    private BiConsumer<Throwable, Object> nothing() {
        return (throwable, o) -> {
        };
    }
}

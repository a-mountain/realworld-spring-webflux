package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ArticleRepository extends ReactiveMongoRepository<Article, String>, ArticleManualRepository {
    Sort MOST_RECENT_SORT = Sort.by(Article.CREATED_AT).descending();

    Flux<Article> findMostRecentByAuthorIdIn(Collection<String> authorId, Pageable pageable);

    Mono<Article> findBySlug(String slug);

    Mono<Article> deleteArticleBySlug(String slug);

    default Flux<Article> findMostRecentArticlesByAuthorIds(Collection<String> authorId, int offset, int limit) {
        return findMostRecentByAuthorIdIn(authorId, OffsetBasedPageable.of(limit, offset, MOST_RECENT_SORT));
    }

    default Mono<Article> findBySlugOrError(String slug) {
        return findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")));
    }
}

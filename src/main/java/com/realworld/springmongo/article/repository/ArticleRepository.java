package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.lib.OffsetBasedPageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ArticleRepository extends ReactiveMongoRepository<Article, String>, ArticleManualRepository {
    Sort NEWEST_ARTICLE_SORT = Sort.by(Article.CREATED_AT_FIELD_NAME).descending();

    Flux<Article> findMostRecentByAuthorIdIn(Collection<String> authorId, Pageable pageable);

    Mono<Article> findBySlug(String slug);

    Mono<Article> deleteArticleBySlug(String slug);

    default Flux<Article> findNewestArticlesByAuthorIds(Collection<String> authorId, int offset, int limit) {
        return findMostRecentByAuthorIdIn(authorId, OffsetBasedPageable.of(limit, offset, NEWEST_ARTICLE_SORT));
    }

    default Mono<Article> findBySlugOrFail(String slug) {
        return findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")));
    }
}

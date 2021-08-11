package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class ArticleManualRepositoryImpl implements ArticleManualRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<Article> findMostRecentArticlesFilteredBy(@Nullable String tag, @Nullable String authorId, @Nullable User favoritedBy, int limit, int offset) {
        var query = new Query()
                .skip(offset)
                .limit(limit)
                .with(ArticleRepository.MOST_RECENT_SORT);
        ofNullable(favoritedBy)
                .ifPresent(user -> query.addCriteria(isFavoriteArticleByUser(user)));
        ofNullable(tag)
                .ifPresent(it -> query.addCriteria(tagsContains(it)));
        ofNullable(authorId)
                .ifPresent(it -> query.addCriteria(authorIdEquals(it)));
        return mongoTemplate.find(query, Article.class);
    }

    private Criteria authorIdEquals(String it) {
        return where(Article.AUTHOR_ID).is(it);
    }

    private Criteria tagsContains(String it) {
        return where(Article.TAGS).all(it);
    }

    private Criteria isFavoriteArticleByUser(User it) {
        return where(Article.ID).in(it.getFavoriteArticleIds());
    }
}

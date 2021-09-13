package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.article.FindArticlesRequest;
import com.realworld.springmongo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public interface ArticleManualRepository {
    Flux<Article> findNewestArticlesFilteredBy(@Nullable String tag,
                                               @Nullable String authorId,
                                               @Nullable User favoritedBy,
                                               int limit,
                                               int offset);

    default Flux<Article> findNewestArticlesFilteredBy(FindArticlesRequest request) {
        return findNewestArticlesFilteredBy(request.getTag(),
                request.getAuthorId(),
                request.getFavoritedBy(),
                request.getLimit(),
                request.getOffset());
    }
}

@RequiredArgsConstructor
class ArticleManualRepositoryImpl implements ArticleManualRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<Article> findNewestArticlesFilteredBy(@Nullable String tag,
                                                      @Nullable String authorId,
                                                      @Nullable User favoritedBy,
                                                      int limit,
                                                      int offset) {
        var query = new Query()
                .skip(offset)
                .limit(limit)
                .with(ArticleRepository.NEWEST_ARTICLE_SORT);
        ofNullable(favoritedBy)
                .ifPresent(user -> query.addCriteria(isFavoriteArticleByUser(user)));
        ofNullable(tag)
                .ifPresent(it -> query.addCriteria(tagsContains(it)));
        ofNullable(authorId)
                .ifPresent(it -> query.addCriteria(authorIdEquals(it)));
        return mongoTemplate.find(query, Article.class);
    }

    private Criteria authorIdEquals(String it) {
        return where(Article.AUTHOR_ID_FIELD_NAME).is(it);
    }

    private Criteria tagsContains(String it) {
        return where(Article.TAGS_FIELD_NAME).all(it);
    }

    private Criteria isFavoriteArticleByUser(User it) {
        return where(Article.ID_FIELD_NAME).in(it.getFavoriteArticleIds());
    }
}

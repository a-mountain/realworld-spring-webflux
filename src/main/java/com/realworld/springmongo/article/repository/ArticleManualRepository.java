package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.article.FindArticlesRequest;
import com.realworld.springmongo.user.User;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

public interface ArticleManualRepository {
    Flux<Article> findMostRecentArticlesFilteredBy(@Nullable String tag, @Nullable String authorId, @Nullable User favoritedBy, int limit, int offset);

    default Flux<Article> findMostRecentArticlesFilteredBy(FindArticlesRequest request) {
        return findMostRecentArticlesFilteredBy(request.getTag(), request.getAuthorId(), request.getFavoritedBy(), request.getLimit(), request.getOffset());
    }
}
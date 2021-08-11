package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleView {
    String slug;

    String title;

    String description;

    String body;

    List<String> tagList;

    Instant createdAt;

    Instant updatedAt;

    Boolean favorited;

    Integer favoritesCount;

    ProfileView author;

    public static ArticleView articleView(Article article, ProfileView author, boolean favorited) {
        return new ArticleView()
                .setSlug(article.getSlug())
                .setTitle(article.getTitle())
                .setDescription(article.getDescription())
                .setBody(article.getBody())
                .setTagList(article.getTags())
                .setCreatedAt(article.getCreatedAt())
                .setUpdatedAt(article.getUpdatedAt())
                .setFavorited(favorited)
                .setFavoritesCount(article.getFavoritesCount())
                .setAuthor(author);
    }

    public static ArticleView articleViewForViewer(Article article, ProfileView author, User user) {
        return articleView(article, author, user.isFavoriteArticle(article));
    }

    public static ArticleView unfavoredArticleView(Article article, ProfileView author) {
        return articleView(article, author, false);
    }
}

package com.realworld.springmongo.user;

import com.realworld.springmongo.article.Article;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Document
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Getter
    @EqualsAndHashCode.Include
    private final String id;

    private final List<String> followingIds;

    private final List<String> favoriteArticleIds;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String encodedPassword;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    @Nullable
    private String bio;

    @Getter
    @Setter
    @Nullable
    private String image;

    @Builder
    public User(String id,
                @Nullable List<String> followingIds,
                @Nullable List<String> favoriteArticleIds,
                String username,
                String encodedPassword,
                String email,
                @Nullable String bio,
                @Nullable String image
    ) {
        this.id = id;
        this.followingIds = ofNullable(followingIds).orElse(new ArrayList<>());
        this.favoriteArticleIds = ofNullable(favoriteArticleIds).orElse(new ArrayList<>());
        this.username = username;
        this.encodedPassword = encodedPassword;
        this.email = email;
        this.bio = bio;
        this.image = image;
    }

    public List<String> getFollowingIds() {
        return Collections.unmodifiableList(followingIds);
    }

    public List<String> getFavoriteArticleIds() {
        return Collections.unmodifiableList(favoriteArticleIds);
    }

    public void follow(String userId) {
        followingIds.add(userId);
    }

    public void unfollow(String userId) {
        followingIds.remove(userId);
    }

    public void follow(User user) {
        follow(user.getId());
    }

    public void unfollow(User user) {
        unfollow(user.getId());
    }

    public void favorite(Article article) {
        article.incrementFavoritesCount();
        favoriteArticleIds.add(article.getId());
    }

    public void unfavorite(Article article) {
        article.decrementFavoritesCount();
        favoriteArticleIds.remove(article.getId());
    }

    public boolean isFavoriteArticle(Article article) {
        return favoriteArticleIds.contains(article.getId());
    }

    public boolean isFollowing(User user) {
        return followingIds.contains(user.getId());
    }

    public boolean isFollower(User user) {
        return user.isFollowing(this);
    }
}

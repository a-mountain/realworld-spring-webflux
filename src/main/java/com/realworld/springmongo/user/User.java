package com.realworld.springmongo.user;

import com.realworld.springmongo.article.Article;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

@Document
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
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
    private String bio;

    @Getter
    @Setter
    private String image;

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

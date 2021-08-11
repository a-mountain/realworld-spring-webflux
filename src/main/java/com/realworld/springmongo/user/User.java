package com.realworld.springmongo.user;

import com.realworld.springmongo.article.Article;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class User {

    @EqualsAndHashCode.Include
    @Getter
    private final String id;

    @Getter
    @Setter
    List<String> followeeIds;

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

    @Getter
    @Setter
    private List<String> favoriteArticleIds;

    public void follow(String userId) {
        followeeIds.add(userId);
    }

    public void unfollow(String userId) {
        followeeIds.remove(userId);
    }

    public void follow(User user) {
        follow(user.getId());
    }

    public void unfollow(User user) {
        unfollow(user.getId());
    }

    public boolean isFavoriteArticle(Article article) {
        return favoriteArticleIds.contains(article.getId());
    }

    public boolean isFollowee(User user) {
        return followeeIds.contains(user.getId());
    }

    public boolean isFollowedBy(User user) {
        return user.isFollowee(this);
    }
}

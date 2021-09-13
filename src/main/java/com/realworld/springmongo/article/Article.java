package com.realworld.springmongo.article;

import com.realworld.springmongo.user.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Document
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Article {

    public static final String CREATED_AT_FIELD_NAME = "createdAt";
    public static final String ID_FIELD_NAME = "id";
    public static final String AUTHOR_ID_FIELD_NAME = "authorId";
    public static final String TAGS_FIELD_NAME = "tags";

    @Getter
    @EqualsAndHashCode.Include
    private final String id;

    @Getter
    private final Instant createdAt;

    @Getter
    @LastModifiedDate
    private final Instant updatedAt;

    @Getter
    private final List<String> tags;

    @Getter
    private final List<Comment> comments;

    @Getter
    private String slug;

    @Getter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String body;

    @Getter
    private Integer favoritesCount;

    @Getter
    @Setter
    private String authorId;

    @Builder
    Article(String id,
            String title,
            String description,
            String body,
            @Nullable Instant createdAt,
            @Nullable Instant updatedAt,
            @Nullable Integer favoritesCount,
            String authorId,
            @Nullable List<String> tags,
            @Nullable List<Comment> comments
    ) {
        this.id = id;
        setTitle(title);
        this.description = description;
        this.body = body;
        this.createdAt = ofNullable(createdAt).orElse(Instant.now());
        this.updatedAt = ofNullable(updatedAt).orElse(createdAt);
        this.favoritesCount = ofNullable(favoritesCount).orElse(0);
        this.authorId = authorId;
        this.tags = ofNullable(tags).orElse(new ArrayList<>());
        this.comments = ofNullable(comments).orElse(new ArrayList<>());
    }

    public void incrementFavoritesCount() {
        favoritesCount++;
    }

    public void decrementFavoritesCount() {
        favoritesCount--;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
    }

    public void setTitle(String title) {
        this.title = title;
        this.slug = toSlug(title);
    }

    public Optional<Comment> getCommentById(String commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst();
    }

    private String toSlug(String title) {
        return title.toLowerCase().replaceAll("[&|\\uFE30-\\uFFA0’”\\s?,.]+", "-");
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean isAuthor(String authorId) {
        return this.authorId.equals(authorId);
    }

    public boolean isAuthor(User author) {
        return isAuthor(author.getId());
    }
}
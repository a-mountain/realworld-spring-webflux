package com.realworld.springmongo.article;

import com.realworld.springmongo.user.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Document
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Article {

    public static final String CREATED_AT = "createdAt";
    public static final String ID = "id";
    public static final String AUTHOR_ID = "authorId";
    public static final String TAGS = "tags";

    @Getter
    @EqualsAndHashCode.Include
    private final String id;

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
    @CreatedDate
    private final Instant createdAt;

    @Getter
    @LastModifiedDate
    private final Instant updatedAt;

    @Getter
    @Setter
    private Integer favoritesCount;

    @Getter
    @Setter
    private String authorId;

    @Getter
    private final List<String> tags;

    @Getter
    private final List<Comment> comments;

    @Builder
    public Article(String id,
                   String title,
                   String description,
                   String body,
                   Instant createdAt,
                   Instant updatedAt,
                   Integer favoritesCount,
                   String authorId,
                   List<String> tags,
                   List<Comment> comments
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.body = body;
        this.createdAt = ofNullable(createdAt).orElse(Instant.now());
        this.updatedAt = updatedAt;
        this.favoritesCount = favoritesCount;
        this.authorId = authorId;
        this.tags = ofNullable(tags).orElse(new ArrayList<>());
        this.comments = ofNullable(comments).orElse(new ArrayList<>());
        updateSlug();
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
    }

    public void deleteComment(String commentId) {
        comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .ifPresent(this::deleteComment);
    }

    public void setTitle(String title) {
        this.title = title;
        updateSlug();
    }

    public Optional<Comment> getCommentById(String commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst();
    }

    private void updateSlug() {
        this.slug = toSlug(title);
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
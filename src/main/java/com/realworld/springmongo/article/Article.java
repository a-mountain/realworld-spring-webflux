package com.realworld.springmongo.article;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

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
    @Transient
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
    @Setter
    private Instant createdAt;

    @Getter
    @Setter
    private Instant updatedAt;

    @Getter
    @Setter
    private Integer favoritesCount;

    @Getter
    @Setter
    private String authorId;

    @Getter
    @Setter
    private List<String> tags;

    @Builder
    public Article(String id, String title, String description, String body, Instant createdAt, Instant updatedAt, Integer favoritesCount, String authorId, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.favoritesCount = favoritesCount;
        this.authorId = authorId;
        this.tags = tags;
        updateSlug();
    }

    public void setTitle(String title) {
        this.title = title;
        updateSlug();
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
}
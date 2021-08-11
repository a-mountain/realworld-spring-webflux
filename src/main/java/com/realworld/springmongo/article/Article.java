package com.realworld.springmongo.article;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Article {

    @Getter
    @EqualsAndHashCode.Include
    private final String id;

    @Getter
    @Setter
    private String slug;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String body;

    @CreatedDate
    @Getter
    @Setter
    private Instant createdAt;

    @LastModifiedDate
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

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean isAuthor(String authorId) {
        return this.authorId.equals(authorId);
    }
}
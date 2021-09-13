package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.article.Article;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateArticleRequest {
    @NotBlank
    String title;

    @NotBlank
    String description;

    @NotBlank
    String body;

    List<String> tagList = Collections.emptyList();

    public Article toArticle(String id, String authorId) {
        return Article.builder()
                .id(id)
                .authorId(authorId)
                .description(description)
                .title(title)
                .body(body)
                .tags(tagList)
                .build();
    }
}

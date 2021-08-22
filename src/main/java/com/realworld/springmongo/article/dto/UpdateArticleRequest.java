package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.validation.NotBlankOrNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateArticleRequest {
    @NotBlankOrNull
    String title;

    @NotBlankOrNull
    String description;

    @NotBlankOrNull
    String body;
}

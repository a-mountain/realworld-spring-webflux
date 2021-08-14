package com.realworld.springmongo.api.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateArticleRequest;
import com.realworld.springmongo.article.dto.UpdateArticleRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ArticleWrapper<T> {
    @JsonProperty("article")
    T content;

    @NoArgsConstructor
    public static class ArticleViewWrapper extends ArticleWrapper<ArticleView> {
        public ArticleViewWrapper(ArticleView article) {
            super(article);
        }
    }

    @NoArgsConstructor
    public static class CreateArticleRequestWrapper extends ArticleWrapper<CreateArticleRequest> {
        public CreateArticleRequestWrapper(CreateArticleRequest article) {
            super(article);
        }
    }

    @NoArgsConstructor
    public static class UpdateArticleRequestWrapper extends ArticleWrapper<UpdateArticleRequest> {
        public UpdateArticleRequestWrapper(UpdateArticleRequest article) {
            super(article);
        }
    }
}

package com.realworld.springmongo.article.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleArticlesDto {
    List<ArticleView> articles;
    int articlesCount;

    public static MultipleArticlesDto of(List<ArticleView> articles) {
        return new MultipleArticlesDto()
                .setArticles(articles)
                .setArticlesCount(articles.size());
    }
}

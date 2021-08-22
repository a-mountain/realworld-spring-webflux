package com.realworld.springmongo.article.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleArticlesView {

    List<ArticleView> articles = Collections.emptyList();

    int articlesCount;

    public static MultipleArticlesView of(List<ArticleView> articles) {
        return new MultipleArticlesView()
                .setArticles(articles)
                .setArticlesCount(articles.size());
    }
}

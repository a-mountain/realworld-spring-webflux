package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ArticleRepository extends ReactiveMongoRepository<Article, String>, ArticleManualRepository {
}

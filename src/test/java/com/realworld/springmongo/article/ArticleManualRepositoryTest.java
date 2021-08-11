package com.realworld.springmongo.article;

import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.user.PasswordService;
import helpers.article.ArticleSamples;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class ArticleManualRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll().block();
    }

    @Test
    void shouldReturnArticlesSortedByCreationTime() {
        createArticles(10);
        var size = 5;

        var actual = articleRepository.findArticles(null, null, null, size, 0)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(size);
        for (int i = 0; i < actual.size(); i++) {
            var article = actual.get(i);
            assertThat(article.getId()).isEqualTo(String.valueOf(i));
        }
    }

    @Test
    void shouldReturnArticlesSortedByCreationTimeWithOffset() {
        createArticles(15);
        var offset = 10;
        var size = 5;

        var actual = articleRepository.findArticles(null, null, null, size, offset)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(size);
        for (int i = 0; i < actual.size(); i++) {
            var article = actual.get(i);
            assertThat(Integer.parseInt(article.getId())).isEqualTo(i + offset);
        }
    }

    @Test
    void shouldReturnArticlesFilteredByTag() {
        var expectedTag = "target_tag";
        var articles = createArticles(8, (article, i) -> {
            if (i < 5) {
                article.tags(List.of(expectedTag, "just_a_tag"));
            }
        });

        var actual = articleRepository.findArticles(expectedTag, null, null, 10, 0)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(5);
        var expected = articles.stream().filter(article -> article.hasTag(expectedTag)).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnArticlesFilteredByAuthor() {
        var expectedAuthor = "new author id";
        var articles = createArticles(10, (article, i) -> {
            if (i > 2 && i <= 5) {
                article.authorId(expectedAuthor);
            }
        });

        var actual = articleRepository.findArticles(null, expectedAuthor, null, 20, 0)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(3);
        var expected = articles.stream().filter(article -> article.isAuthor(expectedAuthor)).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnArticlesFilteredByFavorited() {
        var articles = createArticles(10);
        var user = UserSamples.sampleUser()
                .favoriteArticleIds(List.of("2", "4", "5", "7", "8")).build();

        var actual = articleRepository.findArticles(null, null, user, 20, 0)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(5);
        var expected = articles.stream().filter(user::isFavoriteArticle).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnArticlesFilteredByMultipleFields() {
        var expectedAuthor = "new author id";
        var expectedTag = "target_tag";
        var articles = createArticles(20, (article, i) -> {
            if (i > 10 && i <= 15) {
                article.authorId(expectedAuthor);
                article.tags(List.of(expectedTag, "just_a_tag"));
            }

            if (i < 5) {
                article.authorId(expectedAuthor);
                article.tags(List.of(expectedTag, "just_a_tag"));
            }
        });
        var user = UserSamples.sampleUser(new PasswordService())
                .favoriteArticleIds(List.of("11", "12", "13", "14", "15")).build();

        var actual = articleRepository.findArticles(expectedTag, expectedAuthor, user, 20, 0)
                .collectList()
                .block();

        assert actual != null;
        assertThat(actual).hasSize(5);
        var expected = articles.stream().filter(article -> {
            var hasAuthor = article.isAuthor(expectedAuthor);
            return hasAuthor && user.isFavoriteArticle(article) && article.hasTag(expectedTag);
        }).toList();
        assertThat(actual).isEqualTo(expected);
    }

    private List<Article> createArticles(int size) {
        return createArticles(size, ArticleConfigurer.empty);
    }

    private List<Article> createArticles(int size, ArticleConfigurer articleConfigurer) {
        var articles = IntStream.range(0, size)
                .mapToObj(i -> {
                    sleep();
                    var article = ArticleSamples.sampleArticle().id(String.valueOf(i));
                    articleConfigurer.configure(article, i);
                    return article.build();
                })
                .toList();
        articleRepository.saveAll(articles).blockLast();
        return articles;
    }

    private void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    interface ArticleConfigurer {
        ArticleConfigurer empty = (a, i) -> {
        };

        void configure(Article.ArticleBuilder article, int i);
    }
}
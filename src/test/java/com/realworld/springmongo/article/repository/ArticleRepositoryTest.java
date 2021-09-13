package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.user.PasswordService;
import helpers.article.ArticleSamples;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class ArticleRepositoryTest {

    private final Comparator<Article> mostRecent = Comparator.comparing(Article::getCreatedAt).reversed();
    @Autowired
    ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll().block();
    }


    @Nested
    class FindMostRecentArticlesByAuthorIds {
        @Test
        void shouldReturnArticlesOrderedByMostRecent() {
            var authorIds = List.of("1", "5", "6", "7");
            var expected = createArticles(10, (article, i) -> article.authorId(String.valueOf(i))).stream()
                    .filter(article -> authorIds.contains(article.getAuthorId()))
                    .sorted(mostRecent)
                    .toList();
            var actual = articleRepository.findNewestArticlesByAuthorIds(authorIds, 0, 20).collectList().block();
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesOrderedByMostRecentWithOffsetAndLimit() {
            var authorIds = List.of("1", "5", "6", "7", "3", "4", "8");
            var offset = 2;
            var limit = 3;
            var expected = createArticles(10, (article, i) -> article.authorId(String.valueOf(i))).stream()
                    .filter(article -> authorIds.contains(article.getAuthorId()))
                    .sorted(mostRecent)
                    .skip(offset)
                    .limit(limit)
                    .toList();
            var actual = articleRepository.findNewestArticlesByAuthorIds(authorIds, offset, limit).collectList().block();
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class FindMostRecentArticlesFilteredBy {
        @Test
        void shouldReturnArticlesOrderedByMostRecent() {
            var size = 5;
            var expected = createArticles(10).stream()
                    .sorted(mostRecent)
                    .limit(size)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(null, null, null, size, 0)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesOrderedByMostRecentWithOffset() {
            var offset = 10;
            var size = 5;
            var expected = createArticles(15).stream()
                    .sorted(mostRecent)
                    .skip(offset)
                    .limit(5)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(null, null, null, size, offset)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesFilteredByTag() {
            var offset = 2;
            var expectedTag = "target_tag";
            var articles = createArticles(8, (article, i) -> {
                if (i < 5) {
                    article.tags(List.of(expectedTag, "just_a_tag"));
                }
            });
            var expected = articles.stream()
                    .filter(article -> article.hasTag(expectedTag))
                    .sorted(mostRecent)
                    .skip(offset)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(expectedTag, null, null, 0, offset)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesFilteredByAuthor() {
            var expectedAuthor = "new author id";
            var limit = 20;
            var articles = createArticles(10, (article, i) -> {
                if (i > 2 && i <= 5) {
                    article.authorId(expectedAuthor);
                }
            });
            var expected = articles.stream()
                    .filter(article -> article.isAuthor(expectedAuthor))
                    .sorted(mostRecent)
                    .limit(limit)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(null, expectedAuthor, null, limit, 0)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesFilteredByFavorited() {
            var limit = 20;
            var user = UserSamples.sampleUser()
                    .favoriteArticleIds(List.of("2", "4", "5", "7", "8")).build();
            var expected = createArticles(10).stream()
                    .filter(user::isFavoriteArticle)
                    .sorted(mostRecent)
                    .limit(limit)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(null, null, user, limit, 0)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnArticlesFilteredByMultipleFields() {
            var expectedAuthor = "new author id";
            var expectedTag = "target_tag";
            var user = UserSamples.sampleUser(new PasswordService())
                    .favoriteArticleIds(List.of("11", "12", "13", "14", "15")).build();
            var limit = 20;
            var articles = createArticles(limit, (article, i) -> {
                if (i > 10 && i <= 15) {
                    article.authorId(expectedAuthor);
                    article.tags(List.of(expectedTag, "just_a_tag"));
                }

                if (i < 5) {
                    article.authorId(expectedAuthor);
                    article.tags(List.of(expectedTag, "just_a_tag"));
                }
            });
            var expected = articles.stream()
                    .filter(article -> {
                        var hasAuthor = article.isAuthor(expectedAuthor);
                        return hasAuthor && user.isFavoriteArticle(article) && article.hasTag(expectedTag);
                    })
                    .sorted(mostRecent)
                    .limit(limit)
                    .toList();

            var actual = articleRepository.findNewestArticlesFilteredBy(expectedTag, expectedAuthor, user, limit, 0)
                    .collectList()
                    .block();

            assert actual != null;
            assertThat(actual).isEqualTo(expected);
        }
    }

    private List<Article> createArticles(int size) {
        return createArticles(size, ArticleConfigurer.empty);
    }

    private List<Article> createArticles(int size, ArticleConfigurer articleConfigurer) {
        var articles = IntStream.range(0, size)
                .mapToObj(i -> {
                    var time = Instant.now().plus(i, ChronoUnit.SECONDS);
                    var article = ArticleSamples.sampleArticle()
                            .createdAt(time)
                            .updatedAt(time)
                            .id(String.valueOf(i));
                    articleConfigurer.configure(article, i);
                    return article.build();
                })
                .toList();
        articleRepository.saveAll(articles).blockLast();
        return articles;
    }


    @FunctionalInterface
    interface ArticleConfigurer {
        ArticleConfigurer empty = (a, i) -> {
        };

        void configure(Article.ArticleBuilder article, int i);
    }
}
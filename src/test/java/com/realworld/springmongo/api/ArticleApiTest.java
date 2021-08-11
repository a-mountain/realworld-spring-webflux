package com.realworld.springmongo.api;

import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateArticleRequest;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.user.UserRepository;
import com.realworld.springmongo.user.dto.ProfileView;
import com.realworld.springmongo.user.dto.UserView;
import helpers.article.ArticleApiSupport;
import helpers.article.ArticleSamples;
import helpers.article.FindArticlesRequest;
import helpers.user.UserApiSupport;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArticleApiTest {

    @Autowired
    WebTestClient client;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    UserApiSupport userApi;

    ArticleApiSupport articleApi;

    @BeforeEach
    void setUp() {
        userApi = new UserApiSupport(client);
        articleApi = new ArticleApiSupport(client);
        userRepository.deleteAll().block();
        articleRepository.deleteAll().block();
    }

    @Test
    void shouldCreateArticle() {
        var user = userApi.signup();
        var createArticleRequest = ArticleSamples.sampleCreateArticleRequest();

        var result = articleApi.createArticle(createArticleRequest, user.getToken()).getResponseBody();
        assert result != null;
        var author = result.getAuthor();

        assertThatCreatedArticleIsRight(createArticleRequest, result);
        assertThatCreatedArticleHasRightAuthor(user, author);
        var savedArticles = articleRepository.findAll().collectList().block();
        assertThat(savedArticles).hasSize(1);
    }

    @Test
    void shouldFindArticles() {
        var expectedTag = "tag";
        var preparation = create2UsersAnd3Articles(expectedTag);

        var findArticlesRequest1 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.users.get(0).getUsername());
        var findArticlesRequest2 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.users.get(1).getUsername());

        var articles1 = articleApi.findArticles(findArticlesRequest1).getResponseBody();
        var articles2 = articleApi.findArticles(findArticlesRequest2).getResponseBody();

        assert articles1 != null;
        assert articles2 != null;
        assertThat(articles1.getArticlesCount()).isEqualTo(1);
        assertThat(articles2.getArticlesCount()).isEqualTo(1);

        var article1 = articles1.getArticles().get(0);
        var article2 = articles2.getArticles().get(0);

        assertThat(article1)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.articles.get(0));
        assertThat(article2)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.articles.get(1));
    }

    ArticlesAndUsers create2UsersAnd3Articles(String tag) {
        var user1 = userApi.signup();
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setUsername("test user 2")
                .setEmail("testemail2@gmail.com");
        var user2 = userApi.signup(userRegistrationRequest).getResponseBody();
        assert user2 != null;

        var createArticleRequest1 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest2 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest3 = ArticleSamples.sampleCreateArticleRequest();

        var article1 = articleApi.createArticle(createArticleRequest1, user1.getToken()).getResponseBody();
        var article2 = articleApi.createArticle(createArticleRequest2, user2.getToken()).getResponseBody();
        articleApi.createArticle(createArticleRequest3, user2.getToken());
        assert article1 != null;
        assert article2 != null;
        return new ArticlesAndUsers(List.of(article1, article2), List.of(user1, user2));
    }

    private void assertThatCreatedArticleHasRightAuthor(UserView user, ProfileView author) {
        assertThat(author.getUsername()).isEqualTo(user.getUsername());
        assertThat(author.getBio()).isEqualTo(user.getBio());
        assertThat(author.getImage()).isEqualTo(user.getImage());
        assertThat(author.isFollowing()).isFalse();
    }

    private void assertThatCreatedArticleIsRight(CreateArticleRequest createArticleRequest, ArticleView result) {
        assertThat(result.getBody()).isEqualTo(createArticleRequest.getBody());
        assertThat(result.getDescription()).isEqualTo(createArticleRequest.getDescription());
        assertThat(result.getTitle()).isEqualTo(createArticleRequest.getTitle());
        assertThat(result.getTagList()).isEqualTo(createArticleRequest.getTagList());
    }

    record ArticlesAndUsers(List<ArticleView> articles, List<UserView> users) {
    }
}

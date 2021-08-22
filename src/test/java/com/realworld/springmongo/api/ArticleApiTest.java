package com.realworld.springmongo.api;

import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateArticleRequest;
import com.realworld.springmongo.article.dto.CreateCommentRequest;
import com.realworld.springmongo.article.dto.UpdateArticleRequest;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.article.repository.TagRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleApiTest {

    @Autowired
    WebTestClient client;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TagRepository tagRepository;

    UserApiSupport userApi;

    ArticleApiSupport articleApi;

    @BeforeEach
    void setUp() {
        userApi = new UserApiSupport(client);
        articleApi = new ArticleApiSupport(client);
        userRepository.deleteAll().block();
        articleRepository.deleteAll().block();
        tagRepository.deleteAll().block();
    }

    @Test
    void shouldCreateArticle() {
        var user = userApi.signup();
        var createArticleRequest = ArticleSamples.sampleCreateArticleRequest();

        var result = articleApi.createArticle(createArticleRequest, user.getToken());
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

    @Test
    void shouldReturnFeed() {
        var follower = userApi.signup();
        var followingUserRR = UserSamples.sampleUserRegistrationRequest()
                .setUsername("following username")
                .setEmail("following@gmail.com");
        var followingUser = userApi.signup(followingUserRR);
        assert followingUser != null;
        userApi.follow(followingUser.getUsername(), follower.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), follower.getToken());

        var resultBody = articleApi.feed(follower.getToken(), 1, 2).getResponseBody();

        assert resultBody != null;
        assertThat(resultBody.getArticlesCount()).isEqualTo(2);
        var hasRightAuthor = resultBody.getArticles().stream()
                .map(ArticleView::getAuthor)
                .allMatch(it -> it.getUsername().equals(followingUser.getUsername()));
        assertThat(hasRightAuthor).isTrue();
    }

    @Test
    void shouldReturnArticle() {
        var user = userApi.signup();
        var expected = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest().setTitle("article title"), user.getToken());
        assert expected != null;

        var actual = articleApi.getArticle("article-title", user.getToken());
        assert actual != null;

        assertThat(actual.getSlug()).isEqualTo(expected.getSlug());
    }

    @Test
    void shouldUpdateArticle() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var slug = article.getSlug();
        var updateArticleRequest = new UpdateArticleRequest()
                .setBody("new body")
                .setDescription("new description")
                .setTitle("new title");

        var updatedArticle = articleApi.updateArticle(slug, updateArticleRequest, user.getToken());
        assert updatedArticle != null;

        assertThat(updatedArticle.getAuthor()).isEqualTo(article.getAuthor());
        assertThat(updatedArticle.getBody()).isEqualTo(updateArticleRequest.getBody());
        assertThat(updatedArticle.getDescription()).isEqualTo(updateArticleRequest.getDescription());
        assertThat(updatedArticle.getTitle()).isEqualTo(updateArticleRequest.getTitle());
    }

    @Test
    void shouldDeleteArticle() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var slug = article.getSlug();

        articleApi.deleteArticle(slug, user.getToken());

        var articlesCount = articleRepository.count().block();
        assertThat(articlesCount).isZero();
    }

    @Test
    void shouldAddComment() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");

        var commentView = articleApi.addComment(article.getSlug(), request, user.getToken());
        assert commentView != null;

        assertThat(commentView.getBody()).isEqualTo(request.getBody());
        assertThat(commentView.getAuthor().getUsername()).isEqualTo(user.getUsername());
        var savedArticle = articleRepository.findAll().blockFirst();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isNotEmpty();
    }

    @Test
    void shouldDeleteComment() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");
        var commentView = articleApi.addComment(article.getSlug(), request, user.getToken());
        assert commentView != null;

        articleApi.deleteComment(article.getSlug(), commentView.getId(), user.getToken());

        var savedArticle = articleRepository.findAll().blockFirst();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isEmpty();
    }

    @Test
    void shouldGetComments() {
        var user = userApi.signup();
        userApi.follow(user.getUsername(), user.getToken());
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var comment1 = articleApi.addComment(article.getSlug(), "comment 1", user.getToken());
        var comment2 = articleApi.addComment(article.getSlug(), "comment 2", user.getToken());
        var expectedComments = Set.of(comment1, comment2);

        var actualComments = articleApi.getComments(article.getSlug(), user.getToken()).getResponseBody();

        assertThat(new HashSet<>(actualComments.getComments())).isEqualTo(expectedComments);
    }

    @Test
    void shouldFavoriteArticle() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var favoritedArticle = articleApi.favoriteArticle(article.getSlug(), user);
        assertThat(article.getFavorited()).isFalse();
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(favoritedArticle.getFavoritesCount()).isEqualTo(1);
    }

    @Test
    void shouldUnfavoriteArticle() {
        var user = userApi.signup();
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest(), user.getToken());
        var favoritedArticle = articleApi.favoriteArticle(article.getSlug(), user);
        var unfavoritedArticle = articleApi.unfavoriteArticle(article.getSlug(), user);
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(unfavoritedArticle.getFavorited()).isFalse();
    }

    @Test
    void shouldGetTags() {
        var user = userApi.signup();
        var request1 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of("tag1", "tag2", "tag2"));
        var request2 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of("tag3", "tag4", "tag3"));
        articleApi.createArticle(request1, user.getToken());
        articleApi.createArticle(request2, user.getToken());
        var tagListView = articleApi.getTags().getResponseBody();
        assertThat(tagListView.getTags()).isEqualTo(List.of("tag1", "tag2", "tag3", "tag4"));
    }

    ArticlesAndUsers create2UsersAnd3Articles(String tag) {
        var user1 = userApi.signup();
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setUsername("test user 2")
                .setEmail("testemail2@gmail.com");
        var user2 = userApi.signup(userRegistrationRequest);
        assert user2 != null;

        var createArticleRequest1 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest2 = ArticleSamples.sampleCreateArticleRequest()
                .setTagList(List.of(tag));
        var createArticleRequest3 = ArticleSamples.sampleCreateArticleRequest();

        var article1 = articleApi.createArticle(createArticleRequest1, user1.getToken());
        var article2 = articleApi.createArticle(createArticleRequest2, user2.getToken());
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

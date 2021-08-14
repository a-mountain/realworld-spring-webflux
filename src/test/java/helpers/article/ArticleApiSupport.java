package helpers.article;

import com.realworld.springmongo.api.wrappers.ArticleWrapper.ArticleViewWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.CreateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.UpdateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CommentViewWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CreateCommentRequestWrapper;
import com.realworld.springmongo.article.dto.*;
import com.realworld.springmongo.user.dto.UserView;
import helpers.TokenHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

public class ArticleApiSupport {
    private final WebTestClient client;

    public ArticleApiSupport(WebTestClient client) {
        this.client = client;
    }

    public ArticleView createArticle(CreateArticleRequest createArticleRequest, String authToken) {
        var result = client.post()
                .uri("/api/articles")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(new CreateArticleRequestWrapper(createArticleRequest))
                .exchange()
                .expectBody(ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public EntityExchangeResult<MultipleArticlesView> findArticles(FindArticlesRequest request, String authToken) {
        var requestSpec = client
                .get()
                .uri(builder -> builder
                        .path("/api/articles")
                        .queryParamIfPresent("tag", ofNullable(request.getTag()))
                        .queryParamIfPresent("author", ofNullable(request.getAuthor()))
                        .queryParamIfPresent("favorited", ofNullable(request.getFavorited()))
                        .build()
                );
        if (authToken != null) {
            requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken));
        }
        return requestSpec
                .exchange()
                .expectBody(MultipleArticlesView.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleArticlesView> feed(String authToken, Integer offset, Integer limit) {
        return client.get()
                .uri(builder -> builder.path("/api/articles/feed")
                        .queryParamIfPresent("limit", ofNullable(limit))
                        .queryParamIfPresent("offset", ofNullable(offset))
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(MultipleArticlesView.class)
                .returnResult();
    }

    public ArticleView getArticle(String slug, String authToken) {
        var result = client.get()
                .uri("/api/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public EntityExchangeResult<MultipleArticlesView> findArticles(FindArticlesRequest request) {
        return findArticles(request, null);
    }

    public ArticleView updateArticle(String slug, UpdateArticleRequest updateArticleRequest, String authToken) {
        var result = client.put()
                .uri("/api/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(new UpdateArticleRequestWrapper(updateArticleRequest))
                .exchange()
                .expectBody(ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public CommentView addComment(String articleSlug, CreateCommentRequest request, String authToken) {
        var result = client.post()
                .uri("/api/articles/" + articleSlug + "/comments")
                .bodyValue(new CreateCommentRequestWrapper(request))
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(CommentViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public CommentView addComment(String articleSlug, String body, String authToken) {
        return addComment(articleSlug, new CreateCommentRequest(body), authToken);
    }

    public EntityExchangeResult<Void> deleteComment(String articleSlug, String commentId, String authToken) {
        return client.delete()
                .uri("/api/articles/" + articleSlug + "/comments/" + commentId)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(Void.class)
                .returnResult();
    }

    public EntityExchangeResult<Void> deleteArticle(String slug, String authToken) {
        return client.delete()
                .uri("/api/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(Void.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleCommentsView> getComments(String articleSlug, String authToken) {
        return client.get()
                .uri("/api/articles/" + articleSlug + "/comments")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(MultipleCommentsView.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleCommentsView> getComments(String articleSlug) {
        return client.get()
                .uri("/api/articles/" + articleSlug + "/comments")
                .exchange()
                .expectBody(MultipleCommentsView.class)
                .returnResult();
    }

    public ArticleView favoriteArticle(String articleSlug, UserView user) {
        var result = client.post()
                .uri("/api/articles/{slug}/favorite", articleSlug)
                .headers(authHeader(user))
                .exchange()
                .expectBody(ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ArticleView unfavoriteArticle(String articleSlug, UserView user) {
        var result = client.delete()
                .uri("/api/articles/{slug}/favorite", articleSlug)
                .headers(authHeader(user))
                .exchange()
                .expectBody(ArticleViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }


    public EntityExchangeResult<TagListView> getTags() {
        return client.get()
                .uri("/api/tags")
                .exchange()
                .expectBody(TagListView.class)
                .returnResult();
    }

    private Consumer<HttpHeaders> authHeader(UserView user) {
        return headers -> headers.put(HttpHeaders.AUTHORIZATION, List.of(TokenHelper.formatToken(user.getToken())));
    }
}

package helpers.article;

import com.realworld.springmongo.article.dto.*;
import helpers.TokenHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Optional.ofNullable;

public class ArticleApiSupport {
    private final WebTestClient client;

    public ArticleApiSupport(WebTestClient client) {
        this.client = client;
    }

    public EntityExchangeResult<ArticleView> createArticle(CreateArticleRequest createArticleRequest, String authToken) {
        return client.post()
                .uri("/api/articles")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(createArticleRequest)
                .exchange()
                .expectBody(ArticleView.class)
                .returnResult();
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

    public EntityExchangeResult<ArticleView> getArticle(String slug, String authToken) {
        return client.get()
                .uri("/api/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ArticleView.class)
                .returnResult();
    }

    public EntityExchangeResult<MultipleArticlesView> findArticles(FindArticlesRequest request) {
        return findArticles(request, null);
    }

    public EntityExchangeResult<ArticleView> updateArticle(String slug, UpdateArticleRequest updateArticleRequest, String authToken) {
        return client.put()
                .uri("/api/articles/" + slug)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .bodyValue(updateArticleRequest)
                .exchange()
                .expectBody(ArticleView.class)
                .returnResult();
    }

    public EntityExchangeResult<CommentView> addComment(String articleSlug, CreateCommentRequest request, String authToken) {
        return client.post()
                .uri("/api/articles/" + articleSlug + "/comments")
                .bodyValue(request)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(CommentView.class)
                .returnResult();
    }

    public EntityExchangeResult<CommentView> addComment(String articleSlug, String body, String authToken) {
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
}

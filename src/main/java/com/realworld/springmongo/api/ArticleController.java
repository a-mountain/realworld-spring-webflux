package com.realworld.springmongo.api;

import com.realworld.springmongo.api.wrappers.ArticleWrapper.ArticleViewWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.CreateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.UpdateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CommentViewWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CreateCommentRequestWrapper;
import com.realworld.springmongo.article.ArticleService;
import com.realworld.springmongo.article.dto.MultipleArticlesView;
import com.realworld.springmongo.article.dto.MultipleCommentsView;
import com.realworld.springmongo.article.dto.TagListView;
import com.realworld.springmongo.security.TokenPrincipal;
import com.realworld.springmongo.user.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleService articleService;
    private final UserContext userContext;

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleViewWrapper> createArticle(@RequestBody CreateArticleRequestWrapper request, @AuthenticationPrincipal Mono<TokenPrincipal> tokenPrincipal) {
        return tokenPrincipal.flatMap(principal -> articleService.createArticle(request.getContent(), principal.userId())).map(ArticleViewWrapper::new);
    }

    @GetMapping("/articles")
    public Mono<MultipleArticlesView> getArticles(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "favorited", required = false) String favoritedByUser,
            @RequestParam(value = "author", required = false) String author
    ) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.findArticles(tag, author, favoritedByUser, offset, limit, Optional.of(currentUser)))
                .switchIfEmpty(articleService.findArticles(tag, author, favoritedByUser, offset, limit, Optional.empty()));
    }

    @GetMapping("/articles/feed")
    public Mono<MultipleArticlesView> feed(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.feed(offset, limit, currentUser));
    }

    @GetMapping("/articles/{slug}")
    public Mono<ArticleViewWrapper> getArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.getArticle(slug, Optional.of(currentUser)))
                .switchIfEmpty(articleService.getArticle(slug, Optional.empty())).map(ArticleViewWrapper::new);
    }

    @PutMapping("/articles/{slug}")
    public Mono<ArticleViewWrapper> updateArticle(@RequestBody UpdateArticleRequestWrapper request, @PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.updateArticle(slug, request.getContent(), currentUser)).map(ArticleViewWrapper::new);
    }

    @DeleteMapping("/articles/{slug}")
    public Mono<Void> deleteArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.deleteArticle(slug, currentUser));
    }


    @GetMapping("/articles/{slug}/comments")
    public Mono<MultipleCommentsView> getComments(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.getComments(slug, Optional.of(currentUser)))
                .switchIfEmpty(articleService.getComments(slug, Optional.empty()));
    }

    @PostMapping("/articles/{slug}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentViewWrapper> addComment(@PathVariable String slug, @RequestBody CreateCommentRequestWrapper request) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.addComment(slug, request.getContent(), currentUser)).map(CommentViewWrapper::new);
    }

    @DeleteMapping("/articles/{slug}/comments/{commentId}")
    public Mono<Void> deleteComment(@PathVariable String commentId, @PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.deleteComment(commentId, slug, currentUser));
    }

    @PostMapping("/articles/{slug}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleViewWrapper> favoriteArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.favoriteArticle(slug, currentUser)).map(ArticleViewWrapper::new);
    }


    @DeleteMapping("/articles/{slug}/favorite")
    public Mono<ArticleViewWrapper> unfavoriteArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.unfavoriteArticle(slug, currentUser)).map(ArticleViewWrapper::new);
    }

    @GetMapping("/tags")
    public Mono<TagListView> getTags() {
        return articleService.getTags();
    }
}

package com.realworld.springmongo.api;

import com.realworld.springmongo.article.ArticleService;
import com.realworld.springmongo.article.dto.*;
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
    public Mono<ArticleView> createArticle(@RequestBody CreateArticleRequest request, @AuthenticationPrincipal Mono<TokenPrincipal> tokenPrincipal) {
        return tokenPrincipal.flatMap(principal -> articleService.createArticle(request, principal.userId()));
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
    public Mono<ArticleView> getArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.getArticle(slug, Optional.of(currentUser)))
                .switchIfEmpty(articleService.getArticle(slug, Optional.empty()));
    }

    @PutMapping("/articles/{slug}")
    public Mono<ArticleView> updateArticle(@RequestBody UpdateArticleRequest request, @PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.updateArticle(slug, request, currentUser));
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
    public Mono<CommentView> addComment(@PathVariable String slug, @RequestBody CreateCommentRequest request) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.addComment(slug, request, currentUser));
    }

    @DeleteMapping("/articles/{slug}/comments/{commentId}")
    public Mono<Void> deleteComment(@PathVariable String commentId, @PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.deleteComment(commentId, slug, currentUser));
    }

    @PostMapping("/articles/{slug}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleView> favoriteArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.favoriteArticle(slug, currentUser));
    }


    @DeleteMapping("/articles/{slug}/favorite")
    public Mono<ArticleView> unfavoriteArticle(@PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.unfavoriteArticle(slug, currentUser));
    }
}

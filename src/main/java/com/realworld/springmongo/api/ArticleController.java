package com.realworld.springmongo.api;

import com.realworld.springmongo.article.ArticleService;
import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateArticleRequest;
import com.realworld.springmongo.article.dto.MultipleArticlesView;
import com.realworld.springmongo.article.dto.UpdateArticleRequest;
import com.realworld.springmongo.security.TokenPrincipal;
import com.realworld.springmongo.user.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
                .flatMap(currentUser -> articleService.findArticles(tag, author, favoritedByUser, offset, limit, currentUser))
                .switchIfEmpty(articleService.findArticles(tag, author, favoritedByUser, offset, limit));
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
                .flatMap(currentUser -> articleService.getArticle(slug, currentUser))
                .switchIfEmpty(articleService.getArticle(slug));
    }

    @PutMapping("/articles/{slug}")
    public Mono<ArticleView> updateArticle(@RequestBody UpdateArticleRequest request, @PathVariable String slug) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleService.updateArticle(slug, request, currentUser));
    }

    @DeleteMapping("/articles/{slug}")
    public Mono<Void> deleteArticle(@PathVariable String slug) {
        return articleService.deleteArticle(slug);
    }
}

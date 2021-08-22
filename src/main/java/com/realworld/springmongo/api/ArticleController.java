package com.realworld.springmongo.api;

import com.realworld.springmongo.api.wrappers.ArticleWrapper.ArticleViewWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.CreateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.ArticleWrapper.UpdateArticleRequestWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CommentViewWrapper;
import com.realworld.springmongo.api.wrappers.CommentWrapper.CreateCommentRequestWrapper;
import com.realworld.springmongo.article.ArticleFacade;
import com.realworld.springmongo.article.dto.MultipleArticlesView;
import com.realworld.springmongo.article.dto.MultipleCommentsView;
import com.realworld.springmongo.article.dto.TagListView;
import com.realworld.springmongo.user.UserSessionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleFacade articleFacade;
    private final UserSessionProvider userSessionProvider;

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleViewWrapper> createArticle(@RequestBody CreateArticleRequestWrapper request) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.createArticle(request.getContent(), currentUser))
                .map(ArticleViewWrapper::new);
    }

    @GetMapping("/articles")
    public Mono<MultipleArticlesView> getArticles(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "favorited", required = false) String favoritedByUser,
            @RequestParam(value = "author", required = false) String author
    ) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.findArticles(tag, author, favoritedByUser, offset, limit, Optional.of(currentUser)))
                .switchIfEmpty(articleFacade.findArticles(tag, author, favoritedByUser, offset, limit, Optional.empty()));
    }

    @GetMapping("/articles/feed")
    public Mono<MultipleArticlesView> feed(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.feed(offset, limit, currentUser));
    }

    @GetMapping("/articles/{slug}")
    public Mono<ArticleViewWrapper> getArticle(@PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.getArticle(slug, Optional.of(currentUser)))
                .switchIfEmpty(articleFacade.getArticle(slug, Optional.empty()))
                .map(ArticleViewWrapper::new);
    }

    @PutMapping("/articles/{slug}")
    public Mono<ArticleViewWrapper> updateArticle(@RequestBody UpdateArticleRequestWrapper request, @PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.updateArticle(slug, request.getContent(), currentUser))
                .map(ArticleViewWrapper::new);
    }

    @DeleteMapping("/articles/{slug}")
    public Mono<Void> deleteArticle(@PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.deleteArticle(slug, currentUser));
    }


    @GetMapping("/articles/{slug}/comments")
    public Mono<MultipleCommentsView> getComments(@PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.getComments(slug, Optional.of(currentUser)))
                .switchIfEmpty(articleFacade.getComments(slug, Optional.empty()));
    }

    @PostMapping("/articles/{slug}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentViewWrapper> addComment(@PathVariable String slug, @RequestBody CreateCommentRequestWrapper request) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.addComment(slug, request.getContent(), currentUser))
                .map(CommentViewWrapper::new);
    }

    @DeleteMapping("/articles/{slug}/comments/{commentId}")
    public Mono<Void> deleteComment(@PathVariable String commentId, @PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.deleteComment(commentId, slug, currentUser));
    }

    @PostMapping("/articles/{slug}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleViewWrapper> favoriteArticle(@PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.favoriteArticle(slug, currentUser))
                .map(ArticleViewWrapper::new);
    }


    @DeleteMapping("/articles/{slug}/favorite")
    public Mono<ArticleViewWrapper> unfavoriteArticle(@PathVariable String slug) {
        return userSessionProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> articleFacade.unfavoriteArticle(slug, currentUser))
                .map(ArticleViewWrapper::new);
    }

    @GetMapping("/tags")
    public Mono<TagListView> getTags() {
        return articleFacade.getTags();
    }
}

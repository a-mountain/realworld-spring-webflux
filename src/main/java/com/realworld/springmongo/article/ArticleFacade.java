package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.*;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.article.repository.TagRepository;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ArticleFacade {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final CommentService commentService;
    private final ArticleMapper articleMapper;
    private final ArticlesFinder articlesFinder;

    public Mono<TagListView> getTags() {
        return tagRepository.findAll()
                .collectList()
                .map(TagListView::of);
    }

    public Mono<ArticleView> createArticle(CreateArticleRequest request, User author) {
        var id = UUID.randomUUID().toString();
        var newArticle = request.toArticle(id, author.getId());
        return articleRepository.save(newArticle)
                .flatMap(article -> saveTags(article).thenReturn(article))
                .map(article -> {
                    var profileDto = ProfileView.toUnfollowedProfileView(author);
                    return ArticleView.toUnfavoredArticleView(article, profileDto);
                });
    }

    public Mono<Void> saveTags(Article article) {
        return tagRepository.saveAllTags(article.getTags()).then();
    }

    public Mono<MultipleArticlesView> feed(int offset, int limit, User currentUser) {
        var followingAuthorIds = currentUser.getFollowingIds();
        return articleRepository
                .findNewestArticlesByAuthorIds(followingAuthorIds, offset, limit)
                .flatMap(article -> articleMapper.mapToArticleView(article, currentUser))
                .collectList()
                .map(MultipleArticlesView::of);
    }

    public Mono<MultipleArticlesView> findArticles(String tag, String author, String favoritedByUser, int offset, int limit, Optional<User> currentUser) {
        return articlesFinder.findArticles(tag, author, favoritedByUser, offset, limit, currentUser);
    }

    public Mono<ArticleView> getArticle(String slug, Optional<User> currentUser) {
        return articleRepository.findBySlug(slug)
                .flatMap(article -> articleMapper.mapToArticleView(article, currentUser));
    }

    public Mono<ArticleView> updateArticle(String slug, UpdateArticleRequest request, User currentUser) {
        return articleRepository.findBySlugOrFail(slug)
                .map(article -> updateArticle(request, currentUser, article));
    }

    public Mono<Void> deleteArticle(String slug, User articleAuthor) {
        return articleRepository.findBySlug(slug)
                .flatMap(article -> {
                    if (!article.isAuthor(articleAuthor)) {
                        return Mono.error(new InvalidRequestException("Article", "only author can delete article"));
                    }
                    return articleRepository.deleteArticleBySlug(slug).then();
                });
    }

    public Mono<CommentView> addComment(String slug, CreateCommentRequest request, User currentUser) {
        return commentService.addComment(slug, request, currentUser);
    }

    public Mono<Void> deleteComment(String commentId, String slug, User user) {
        return commentService.deleteComment(commentId, slug, user);
    }

    public Mono<MultipleCommentsView> getComments(String slug, Optional<User> user) {
        return commentService.getComments(slug, user);
    }

    public Mono<ArticleView> favoriteArticle(String slug, User currentUser) {
        return articleRepository.findBySlug(slug)
                .map(article -> {
                    currentUser.favorite(article);
                    return ArticleView.ofOwnArticle(article, currentUser);
                });
    }

    public Mono<ArticleView> unfavoriteArticle(String slug, User currentUser) {
        return articleRepository.findBySlug(slug)
                .map(article -> {
                    currentUser.unfavorite(article);
                    return ArticleView.ofOwnArticle(article, currentUser);
                });
    }

    private ArticleView updateArticle(UpdateArticleRequest request, User currentUser, Article article) {
        if (!article.isAuthor(currentUser)) {
            throw new InvalidRequestException("Article", "only author can update article");
        }

        ofNullable(request.getBody())
                .ifPresent(article::setBody);
        ofNullable(request.getDescription())
                .ifPresent(article::setDescription);
        ofNullable(request.getTitle())
                .ifPresent(article::setTitle);
        return ArticleView.ofOwnArticle(article, currentUser);
    }
}

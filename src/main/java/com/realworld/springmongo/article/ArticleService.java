package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.*;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserRepository;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static com.realworld.springmongo.article.dto.ArticleView.toArticleViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.toProfileViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.toUnfollowedProfileView;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Mono<ArticleView> createArticle(CreateArticleRequest request, String authorId) {
        var id = UUID.randomUUID().toString();
        var article = request.toArticle(id, authorId);
        return articleRepository
                .save(article)
                .zipWith(userRepository.findById(authorId), (theArticle, user) -> {
                    var profileDto = ProfileView.toUnfollowedProfileView(user);
                    return ArticleView.toUnfavoredArticleView(theArticle, profileDto);
                });
    }

    public Mono<MultipleArticlesView> feed(int offset, int limit, User currentUser) {
        var followingAuthorIds = currentUser.getFollowingIds();
        return articleRepository
                .findMostRecentArticlesByAuthorIds(followingAuthorIds, offset, limit)
                .flatMap(article -> toArticleViewForUser(article, currentUser))
                .collectList()
                .map(MultipleArticlesView::of);
    }

    public Mono<MultipleArticlesView> findArticles(String tag, String author, String favoritedByUser, int offset, int limit, @Nullable User currentUser) {
        return createFindArticleRequest(tag, author, favoritedByUser, offset, limit)
                .flatMapMany(articleRepository::findMostRecentArticlesFilteredBy)
                .flatMap(article -> currentUser != null ? toArticleViewForUser(article, currentUser) : toArticleView(article))
                .collectList()
                .map(MultipleArticlesView::of);
    }

    public Mono<MultipleArticlesView> findArticles(String tag, String author, String favoritedByUser, int offset, int limit) {
        return findArticles(tag, author, favoritedByUser, offset, limit, null);
    }

    public Mono<FindArticlesRequest> createFindArticleRequest(String tag, String author, String favoritedByUser, int offset, int limit) {
        var request = new FindArticlesRequest()
                .setOffset(offset)
                .setLimit(limit)
                .setTag(tag);
        Mono<?> setAuthorId = getAuthorId(author).doOnNext(request::setAuthorId);
        Mono<?> setFavoritedBy = getFavoritedBy(favoritedByUser).doOnNext(request::setFavoritedBy);
        return setAuthorId
                .then(setFavoritedBy)
                .thenReturn(request);
    }

    private Mono<ArticleView> toArticleViewForUser(Article article, User user) {
        return userRepository.findById(article.getAuthorId())
                .map(it -> toArticleViewForViewer(article, toProfileViewForViewer(it, user), user));
    }

    private Mono<ArticleView> toArticleView(Article article) {
        return userRepository.findById(article.getAuthorId())
                .map(author -> ArticleView.toUnfavoredArticleView(article, toUnfollowedProfileView(author)));
    }

    private Mono<String> getAuthorId(String author) {
        if (author == null) {
            return Mono.empty();
        }
        return userRepository.findByUsername(author).map(User::getId);
    }

    private Mono<User> getFavoritedBy(String favoritedBy) {
        if (favoritedBy == null) {
            return Mono.empty();
        }
        return userRepository.findByUsername(favoritedBy);
    }

    public Mono<ArticleView> getArticle(String slug, User currentUser) {
        return articleRepository.findBySlug(slug)
                .flatMap(article -> toArticleViewForUser(article, currentUser));
    }

    public Mono<ArticleView> getArticle(String slug) {
        return articleRepository
                .findBySlug(slug)
                .flatMap(this::toArticleView);
    }

    public Mono<ArticleView> updateArticle(String slug, UpdateArticleRequest request, User currentUser) {
        return articleRepository.findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")))
                .map(article -> {
                    ofNullable(request.getBody())
                            .ifPresent(article::setBody);
                    ofNullable(request.getDescription())
                            .ifPresent(article::setDescription);
                    ofNullable(request.getTitle())
                            .ifPresent(article::setTitle);
                    return ArticleView.toOwnArticleView(article, currentUser);
                });
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
        return articleRepository.findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")))
                .flatMap(article -> {
                    var comment = request.toComment(UUID.randomUUID().toString(), currentUser.getId());
                    article.addComment(comment);
                    var profileView = CommentView.toCommentView(comment, ProfileView.toOwnProfile(currentUser));
                    return articleRepository.save(article).thenReturn(profileView);
                });
    }

    public Mono<Void> deleteComment(String commentId, String slug, User user) {
        return articleRepository.findBySlug(slug)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Article", "not found")))
                .flatMap(article -> article.getCommentById(commentId)
                        .map(comment -> deleteComment(article, comment, user))
                        .orElse(Mono.empty())
                );
    }

    public Mono<Void> deleteComment(Article article, Comment comment, User user) {
        if (!comment.isAuthor(user)) {
            return Mono.error(new InvalidRequestException("Comment", "only author can delete comment"));
        }
        article.deleteComment(comment);
        return articleRepository.save(article).then();
    }
}

package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.CommentView;
import com.realworld.springmongo.article.dto.CreateCommentRequest;
import com.realworld.springmongo.article.dto.MultipleCommentsView;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserRepository;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static com.realworld.springmongo.user.dto.ProfileView.toProfileViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.toUnfollowedProfileView;

@Component
@RequiredArgsConstructor
class CommentService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Mono<CommentView> addComment(String slug, CreateCommentRequest request, User currentUser) {
        return articleRepository.findBySlugOrFail(slug)
                .flatMap(article -> addComment(request, currentUser, article));
    }

    public Mono<Void> deleteComment(String commentId, String slug, User user) {
        return articleRepository.findBySlugOrFail(slug)
                .flatMap(article -> article.getCommentById(commentId)
                        .map(comment -> deleteComment(article, comment, user))
                        .orElse(Mono.empty())
                );
    }

    public Mono<MultipleCommentsView> getComments(String slug, Optional<User> user) {
        return articleRepository.findBySlug(slug)
                .zipWhen(article -> userRepository.findById(article.getAuthorId()))
                .map(tuple -> {
                    var article = tuple.getT1();
                    var author = tuple.getT2();
                    return getComments(user, article, author);
                });
    }

    private Mono<Void> deleteComment(Article article, Comment comment, User user) {
        if (!comment.isAuthor(user)) {
            return Mono.error(new InvalidRequestException("Comment", "only author can delete comment"));
        }
        article.deleteComment(comment);
        return articleRepository.save(article).then();
    }

    private Mono<CommentView> addComment(CreateCommentRequest request, User currentUser, Article article) {
        var comment = request.toComment(UUID.randomUUID().toString(), currentUser.getId());
        article.addComment(comment);
        var profileView = CommentView.toCommentView(comment, ProfileView.toOwnProfile(currentUser));
        return articleRepository.save(article).thenReturn(profileView);
    }

    private MultipleCommentsView getComments(Optional<User> user, Article article, User author) {
        var comments = article.getComments();
        var authorProfile = user
                .map(viewer -> toProfileViewForViewer(author, viewer))
                .orElse(toUnfollowedProfileView(author));
        var commentViews = comments.stream()
                .map(comment -> CommentView.toCommentView(comment, authorProfile))
                .toList();
        return MultipleCommentsView.of(commentViews);
    }
}

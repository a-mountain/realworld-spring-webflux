package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.realworld.springmongo.article.dto.ArticleView.toArticleViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.toProfileViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.toUnfollowedProfileView;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    private final UserRepository userRepository;

    public Mono<ArticleView> mapToArticleView(Article article, Optional<User> viewer) {
        return viewer.map(user -> mapToArticleView(article, user)).orElseGet(() -> mapToArticleView(article));
    }

    public Mono<ArticleView> mapToArticleView(Article article, User user) {
        return userRepository.findAuthorByArticle(article)
                .map(it -> toArticleViewForViewer(article, toProfileViewForViewer(it, user), user));
    }

    public Mono<ArticleView> mapToArticleView(Article article) {
        return userRepository.findAuthorByArticle(article)
                .map(author -> ArticleView.toUnfavoredArticleView(article, toUnfollowedProfileView(author)));
    }
}

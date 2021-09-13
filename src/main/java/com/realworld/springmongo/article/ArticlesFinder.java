package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.MultipleArticlesView;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class ArticlesFinder {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    public Mono<MultipleArticlesView> findArticles(String tag, String author, String favoritedByUser, int offset, int limit, Optional<User> currentUser) {
        return createFindArticleRequest(tag, author, favoritedByUser, offset, limit)
                .flatMapMany(articleRepository::findNewestArticlesFilteredBy)
                .flatMap(article -> articleMapper.mapToArticleView(article, currentUser))
                .collectList()
                .map(MultipleArticlesView::of);
    }

    private Mono<FindArticlesRequest> createFindArticleRequest(String tag, String author, String favoritedByUser, int offset, int limit) {
        var request = new FindArticlesRequest()
                .setOffset(offset)
                .setLimit(limit)
                .setTag(tag);
        return addToRequestAuthorId(author, request)
                .then(addToRequestFavoritedBy(favoritedByUser, request))
                .thenReturn(request);
    }

    private Mono<User> addToRequestFavoritedBy(String favoritedByUser, FindArticlesRequest request) {
        return getFavoritedBy(favoritedByUser).doOnNext(request::setFavoritedBy);
    }

    private Mono<String> addToRequestAuthorId(String author, FindArticlesRequest request) {
        return getAuthorId(author).doOnNext(request::setAuthorId);
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
}

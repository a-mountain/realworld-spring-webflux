package com.realworld.springmongo.article;

import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateArticleRequest;
import com.realworld.springmongo.article.dto.MultipleArticlesView;
import com.realworld.springmongo.article.repository.ArticleRepository;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserRepository;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.realworld.springmongo.user.dto.ProfileView.profileViewForViewer;
import static com.realworld.springmongo.user.dto.ProfileView.unfollowedProfileView;

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
                    var profileDto = ProfileView.unfollowedProfileView(user);
                    return ArticleView.unfavoredArticleView(theArticle, profileDto);
                });
    }

    public Mono<MultipleArticlesView> feed(int offset, int limit, User currentUser) {
        var followingAuthorIds = currentUser.getFollowingIds();
        return articleRepository
                .findMostRecentArticlesByAuthorIds(followingAuthorIds, offset, limit)
                .flatMap(article -> getArticleViewForUser(article, currentUser))
                .collectList()
                .map(MultipleArticlesView::of);
    }

    public Mono<MultipleArticlesView> findArticles(String tag, String author, String favoritedByUser, int offset, int limit, @Nullable User currentUser) {
        return createFindArticleRequest(tag, author, favoritedByUser, offset, limit)
                .flatMapMany(articleRepository::findMostRecentArticlesFilteredBy)
                .flatMap(article -> currentUser != null ? getArticleViewForUser(article, currentUser) : getArticleView(article))
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

    private Mono<ArticleView> getArticleViewForUser(Article article, User user) {
        return userRepository.findById(article.getAuthorId())
                .map(it -> ArticleView.articleViewForViewer(article, profileViewForViewer(it, user), user));
    }

    private Mono<ArticleView> getArticleView(Article article) {
        return userRepository.findById(article.getAuthorId())
                .map(author -> ArticleView.unfavoredArticleView(article, unfollowedProfileView(author)));
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

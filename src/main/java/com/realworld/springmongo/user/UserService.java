package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CredentialsService credentialsService;
    private final UserRepository userRepository;
    private final UserUpdater userUpdater;

    public Mono<ProfileView> getProfile(String profileUsername, User viewer) {
        return findUserByUsernameOrError(profileUsername)
                .map(user -> ProfileView.toProfileViewForViewer(user, viewer));
    }

    public Mono<ProfileView> getProfile(String profileUsername) {
        return findUserByUsernameOrError(profileUsername)
                .map(user -> ProfileView.toProfileView(user, false));
    }

    public Mono<UserView> signup(UserRegistrationRequest request) {
        return credentialsService.signup(request);
    }

    public Mono<UserView> login(UserAuthenticationRequest request) {
        return credentialsService.login(request);
    }

    public Mono<UserView> getCurrentUser() {
        return credentialsService.getCurrentUserAndToken()
                .map(UserView::fromUserAndToken);
    }

    public Mono<UserView> updateUser(UpdateUserRequest request, UserContext.UserAndToken userAndToken) {
        var user = userAndToken.user();
        var token = userAndToken.token();
        return userUpdater.updateUser(request, user)
                .flatMap(userRepository::save)
                .map(it -> UserView.fromUserAndToken(it, token));
    }

    public Mono<ProfileView> follow(String username, User follower) {
        return findUserByUsernameOrError(username)
                .flatMap(userToFollow -> {
                    follower.follow(userToFollow);
                    return userRepository.save(follower).thenReturn(userToFollow);
                })
                .map(ProfileView::toFollowedProfileViewOf);
    }

    public Mono<ProfileView> unfollow(String username, User follower) {
        return findUserByUsernameOrError(username)
                .flatMap(userToUnfollow -> {
                    follower.unfollow(userToUnfollow);
                    return userRepository.save(follower).thenReturn(userToUnfollow);
                })
                .map(ProfileView::toUnfollowedProfileView);
    }

    private Mono<User> findUserByUsernameOrError(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(usernameNotFoundException());
    }

    private <T> Mono<T> usernameNotFoundException() {
        return Mono.error(new InvalidRequestException("Username", "not found"));
    }
}

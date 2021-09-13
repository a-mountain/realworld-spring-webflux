package com.realworld.springmongo.user;

import com.realworld.springmongo.user.UserSessionProvider.UserSession;
import com.realworld.springmongo.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final CredentialsService credentialsService;
    private final UserRepository userRepository;
    private final UserUpdater userUpdater;

    public Mono<ProfileView> getProfile(String profileUsername, User viewer) {
        return userRepository.findByUsernameOrFail(profileUsername)
                .map(user -> ProfileView.toProfileViewForViewer(user, viewer));
    }

    public Mono<ProfileView> getProfile(String profileUsername) {
        return userRepository.findByUsernameOrFail(profileUsername)
                .map(ProfileView::toUnfollowedProfileView);
    }

    public Mono<UserView> signup(UserRegistrationRequest request) {
        return credentialsService.signup(request);
    }

    public Mono<UserView> login(UserAuthenticationRequest request) {
        return credentialsService.login(request);
    }

    public Mono<UserView> updateUser(UpdateUserRequest request, UserSession userSession) {
        var user = userSession.user();
        var token = userSession.token();
        return userUpdater.updateUser(request, user)
                .flatMap(userRepository::save)
                .map(it -> UserView.fromUserAndToken(it, token));
    }

    public Mono<ProfileView> follow(String username, User follower) {
        return userRepository.findByUsernameOrFail(username)
                .flatMap(userToFollow -> {
                    follower.follow(userToFollow);
                    return userRepository.save(follower).thenReturn(userToFollow);
                })
                .map(ProfileView::toFollowedProfileView);
    }

    public Mono<ProfileView> unfollow(String username, User follower) {
        return userRepository.findByUsernameOrFail(username)
                .flatMap(userToUnfollow -> {
                    follower.unfollow(userToUnfollow);
                    return userRepository.save(follower).thenReturn(userToUnfollow);
                })
                .map(ProfileView::toUnfollowedProfileView);
    }
}

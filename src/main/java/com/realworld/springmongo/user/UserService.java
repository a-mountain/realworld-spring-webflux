package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.security.TokenPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.UUID;

import static com.realworld.springmongo.helpers.Helpers.doIfPresent;
import static java.util.function.Function.identity;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserTokenProvider tokenProvider;
    private final PasswordService passwordService;
    private final UserRepository userRepository;

    public Mono<ProfileDto> getProfile(String followeeUsername, String followerUserId) {
        return userRepository.existsByUsername(followeeUsername)
                .flatMap(existsByUsername -> {
                    if (!existsByUsername) {
                        return Mono.error(usernameNotFoundException());
                    }
                    return findProfile(followeeUsername, followerUserId);
                });
    }

    private Mono<ProfileDto> findProfile(String followeeUsername, String followerUserId) {
        return userRepository.findByUsername(followeeUsername)
                .flatMap(user -> {
                    var profiledFollowed = isProfiledFollowed(followerUserId, user.getId());
                    return profiledFollowed.map(isProfiledFollowed -> Tuples.of(user, isProfiledFollowed));
                })
                .map(tuple -> ProfileDto.fromUser(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Boolean> isProfiledFollowed(String followerId, String followeeId) {
        if (followerId.isEmpty()) {
            return Mono.just(false);
        }
        return userRepository.existsByIdAndFolloweeIdsContains(followerId, List.of(followeeId));
    }

    public Mono<UserWithToken> signup(UserRegistrationRequest request) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(existsByEmail -> {
                    if (existsByEmail) {
                        return Mono.error(emailAlreadyInUseException());
                    }
                    return userRepository.existsByUsername(request.getUsername());
                })
                .flatMap(existsByUsername -> {
                    if (existsByUsername) {
                        return Mono.error(usernameAlreadyInUseException());
                    }
                    return registerNewUser(request);
                });
    }

    private Mono<UserWithToken> registerNewUser(UserRegistrationRequest request) {
        var rowPassword = request.getPassword();
        var encodedPassword = passwordService.encodePassword(rowPassword);
        var id = UUID.randomUUID().toString();
        var user = request.toUser(encodedPassword, id);
        return userRepository
                .save(user)
                .map(this::createAuthenticationResponse);
    }

    public Mono<UserWithToken> login(UserAuthenticationRequest request) {
        var email = request.getEmail();
        var password = request.getPassword();
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Email", "not found")))
                .map(user -> loginUser(password, user));
    }

    private UserWithToken loginUser(String password, User user) {
        var encodedPassword = user.getEncodedPassword();
        if (!passwordService.matchesRowPasswordWithEncodedPassword(password, encodedPassword)) {
            throw new InvalidRequestException("Password", "invalid");
        }
        return createAuthenticationResponse(user);
    }

    private UserWithToken createAuthenticationResponse(User user) {
        var token = tokenProvider.getToken(user.getId());
        return UserWithToken.fromUser(user, token);
    }

    public Mono<UserWithToken> getCurrentUser(Mono<TokenPrincipal> principalMono) {
        return principalMono
                .flatMap(principal -> userRepository.findById(principal.userId()))
                .zipWith(principalMono, (user, principal) -> UserWithToken.fromUser(user, principal.token()));
    }

    public Mono<UserWithToken> updateUser(UpdateUserRequest request, Mono<TokenPrincipal> principalMono) {
        return principalMono
                .flatMap(principal -> userRepository.findById(principal.userId()))
                .flatMap(user -> updateUserFields(request, user))
                .flatMap(userRepository::save)
                .zipWith(principalMono, (user, principal) -> UserWithToken.fromUser(user, principal.token()));
    }

    private Mono<User> updateUserFields(UpdateUserRequest request, User user) {
        doIfPresent(request.getBio(), user::setBio);
        doIfPresent(request.getImage(), user::setImage);
        doIfPresent(request.getPassword(), () -> updatePassword(request, user));
        Mono<?> updateUsername = updateUsername(request, user);
        Mono<?> updateEmail = updateEmail(request, user);
        return updateUsername.then(updateEmail).thenReturn(user);
    }

    private void updatePassword(UpdateUserRequest request, User user) {
        var encodePassword = passwordService.encodePassword(request.getPassword());
        user.setEncodedPassword(encodePassword);
    }

    private Mono<Boolean> updateUsername(UpdateUserRequest request, User user) {
        if (request.getUsername() == null) {
            return Mono.empty();
        }
        return userRepository.existsByUsername(request.getUsername())
                .doOnNext(existsByUsername -> {
                    if (existsByUsername) {
                        throw usernameAlreadyInUseException();
                    }
                    user.setUsername(request.getUsername());
                });
    }

    private Mono<Boolean> updateEmail(UpdateUserRequest request, User user) {
        if (request.getEmail() == null) {
            return Mono.empty();
        }
        return userRepository.existsByEmail(request.getEmail())
                .doOnNext(existsByEmail -> {
                    if (existsByEmail) {
                        throw emailAlreadyInUseException();
                    }
                    user.setEmail(request.getEmail());
                });
    }

    public Mono<ProfileDto> follow(String username, String followerId) {
        var followeeMono = userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(usernameNotFoundException()));
        var followerMono = userRepository.findById(followerId);
        return followeeMono
                .zipWith(followerMono, this::followUser)
                .flatMap(identity())
                .map(user -> ProfileDto.fromUser(user, true));
    }

    private Mono<User> followUser(User user, User follower) {
        follower.followeeIds.add(user.getId());
        return userRepository.save(follower).thenReturn(user);
    }

    public Mono<ProfileDto> unfollow(String username, String followerId) {
        var followeeMono = userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(usernameNotFoundException()));
        var followerMono = userRepository.findById(followerId);
        return followeeMono
                .zipWith(followerMono, this::unfollowUser)
                .flatMap(identity())
                .map(user -> ProfileDto.fromUser(user, false));
    }

    private Mono<User> unfollowUser(User user, User follower) {
        follower.followeeIds.remove(user.getId());
        return userRepository.save(follower).thenReturn(user);
    }

    private InvalidRequestException usernameAlreadyInUseException() {
        return new InvalidRequestException("Username", "already in use");
    }

    private InvalidRequestException emailAlreadyInUseException() {
        return new InvalidRequestException("Email", "already in use");
    }

    private InvalidRequestException usernameNotFoundException() {
        return new InvalidRequestException("Username", "not found");
    }
}

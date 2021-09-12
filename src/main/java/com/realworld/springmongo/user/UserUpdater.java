package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
class UserUpdater {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public Mono<User> updateUser(UpdateUserRequest request, User user) {
        ofNullable(request.getBio())
                .ifPresent(user::setBio);
        ofNullable(request.getImage())
                .ifPresent(user::setImage);
        ofNullable(request.getPassword())
                .ifPresent(password -> updatePassword(password, user));
        return updateUsername(request, user)
                .then(updateEmail(request, user))
                .thenReturn(user);
    }

    private void updatePassword(String password, User user) {
        var encodedPassword = passwordService.encodePassword(password);
        user.setEncodedPassword(encodedPassword);
    }

    private Mono<?> updateUsername(UpdateUserRequest request, User user) {
        if (request.getUsername() == null) {
            return Mono.empty();
        }
        if (request.getUsername().equals(user.getUsername())) {
            return Mono.empty();
        }
        return userRepository.existsByUsername(request.getUsername())
                .doOnNext(existsByUsername -> updateUsername(request, user, existsByUsername));
    }

    private void updateUsername(UpdateUserRequest request, User user, boolean existsByUsername) {
        if (existsByUsername) {
            throw usernameAlreadyInUseException();
        }
        user.setUsername(request.getUsername());
    }

    private Mono<?> updateEmail(UpdateUserRequest request, User user) {
        if (request.getEmail() == null) {
            return Mono.empty();
        }
        if (request.getEmail().equals(user.getEmail())) {
            return Mono.empty();
        }
        return userRepository.existsByEmail(request.getEmail())
                .doOnNext(existsByEmail -> updateEmail(request, user, existsByEmail));
    }

    private void updateEmail(UpdateUserRequest request, User user, boolean existsByEmail) {
        if (existsByEmail) {
            throw emailAlreadyInUseException();
        }
        user.setEmail(request.getEmail());
    }

    private InvalidRequestException usernameAlreadyInUseException() {
        return new InvalidRequestException("Username", "already in use");
    }

    private InvalidRequestException emailAlreadyInUseException() {
        return new InvalidRequestException("Email", "already in use");
    }
}

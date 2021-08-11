package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class UserUpdater {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public Mono<User> updateUser(UpdateUserRequest request, User user) {
        ofNullable(request.getBio())
                .ifPresent(user::setBio);
        ofNullable(request.getImage())
                .ifPresent(user::setImage);
        ofNullable(request.getPassword())
                .ifPresent(password -> updatePassword(password, user));
        Mono<?> updateUsername = updateUsername(request, user);
        Mono<?> updateEmail = updateEmail(request, user);
        return updateUsername
                .then(updateEmail)
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
        return userRepository.existsByUsername(request.getUsername())
                .doOnNext(existsByUsername -> {
                    if (existsByUsername) {
                        throw usernameAlreadyInUseException();
                    }
                    user.setUsername(request.getUsername());
                });
    }

    private Mono<?> updateEmail(UpdateUserRequest request, User user) {
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

    private InvalidRequestException usernameAlreadyInUseException() {
        return new InvalidRequestException("Username", "already in use");
    }

    private InvalidRequestException emailAlreadyInUseException() {
        return new InvalidRequestException("Email", "already in use");
    }
}

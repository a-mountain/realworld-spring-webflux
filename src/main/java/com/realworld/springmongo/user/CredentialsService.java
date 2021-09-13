package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.user.dto.UserAuthenticationRequest;
import com.realworld.springmongo.user.dto.UserRegistrationRequest;
import com.realworld.springmongo.user.dto.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class CredentialsService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserTokenProvider tokenProvider;

    public Mono<UserView> login(UserAuthenticationRequest request) {
        var email = request.getEmail();
        var password = request.getPassword();
        return userRepository.findByEmailOrFail(email)
                .map(user -> loginUser(password, user));
    }

    public Mono<UserView> signup(UserRegistrationRequest request) {
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

    private UserView loginUser(String password, User user) {
        var encodedPassword = user.getEncodedPassword();
        if (!passwordService.matchesRowPasswordWithEncodedPassword(password, encodedPassword)) {
            throw new InvalidRequestException("Password", "invalid");
        }
        return createAuthenticationResponse(user);
    }

    private UserView createAuthenticationResponse(User user) {
        var token = tokenProvider.getToken(user.getId());
        return UserView.fromUserAndToken(user, token);
    }

    private Mono<UserView> registerNewUser(UserRegistrationRequest request) {
        var rowPassword = request.getPassword();
        var encodedPassword = passwordService.encodePassword(rowPassword);
        var id = UUID.randomUUID().toString();
        var user = request.toUser(encodedPassword, id);
        return userRepository
                .save(user)
                .map(this::createAuthenticationResponse);
    }

    private InvalidRequestException usernameAlreadyInUseException() {
        return new InvalidRequestException("Username", "already in use");
    }

    private InvalidRequestException emailAlreadyInUseException() {
        return new InvalidRequestException("Email", "already in use");
    }
}

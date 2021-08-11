package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.security.TokenPrincipal;
import com.realworld.springmongo.user.dto.UserAuthenticationRequest;
import com.realworld.springmongo.user.dto.UserRegistrationRequest;
import com.realworld.springmongo.user.dto.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CredentialsService implements UserContext {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserTokenProvider tokenProvider;

    @Override
    public Mono<User> getCurrentUserOrEmpty() {
        return getCurrentUserAndToken()
                .map(UserAndToken::user);
    }

    @Override
    public Mono<UserAndToken> getCurrentUserAndToken() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    var authentication = context.getAuthentication();
                    if (authentication == null) {
                        return Mono.empty();
                    }
                    var tokenPrincipal = (TokenPrincipal) authentication.getPrincipal();
                    return userRepository
                            .findById(tokenPrincipal.userId())
                            .map(user -> new UserContext.UserAndToken(user, tokenPrincipal.token()));
                });
    }

    public Mono<UserView> login(UserAuthenticationRequest request) {
        var email = request.getEmail();
        var password = request.getPassword();
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Email", "not found")))
                .map(user -> loginUser(password, user));
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

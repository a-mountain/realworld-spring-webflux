package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserTokenProvider tokenProvider;
    private final PasswordService passwordService;
    private final UserRepository userRepository;

    public Mono<UserAuthenticationResponse> signup(UserRegistrationRequest request) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(existsByEmail -> {
                    if (existsByEmail) {
                        return Mono.error(new InvalidRequestException("Email", "already in use"));
                    }
                    return userRepository.existsByUsername(request.getUsername());
                })
                .flatMap(existsByUsername -> {
                    if (existsByUsername) {
                        return Mono.error(new InvalidRequestException("Username", "already in use"));
                    }
                    return registerNewUser(request);
                });
    }

    private Mono<UserAuthenticationResponse> registerNewUser(UserRegistrationRequest request) {
        var rowPassword = request.getPassword();
        var encodedPassword = passwordService.encodePassword(rowPassword);
        var id = UUID.randomUUID().toString();
        var user = request.toUser(encodedPassword, id);
        return userRepository
                .save(user)
                .map(this::createAuthenticationResponse);
    }

    public Mono<UserAuthenticationResponse> login(UserAuthenticationRequest request) {
        var email = request.getEmail();
        var password = request.getPassword();
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Email", "not found")))
                .map(user -> loginUser(password, user));
    }

    private UserAuthenticationResponse loginUser(String password, User user) {
        var encodedPassword = user.getEncodedPassword();
        if (!passwordService.matchesRowPasswordWithEncodedPassword(password, encodedPassword)) {
            throw new InvalidRequestException("Password", "invalid");
        }
        return createAuthenticationResponse(user);
    }

    private UserAuthenticationResponse createAuthenticationResponse(User user) {
        var token = tokenProvider.getToken(user.getId());
        return UserAuthenticationResponse.fromUser(user, token);
    }
}

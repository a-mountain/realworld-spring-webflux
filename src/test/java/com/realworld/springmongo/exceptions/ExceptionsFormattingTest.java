package com.realworld.springmongo.exceptions;

import com.realworld.springmongo.validation.LocaleConfigurer;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@WebFluxTest(controllers = ExceptionsFormattingTest.Controller.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import({LocaleConfigurer.class, ExceptionsFormattingTest.Controller.class})
class ExceptionsFormattingTest {

    @Autowired
    WebTestClient client;

    @Test
    void shouldFormatInvalidRequestException() {
        client.get()
                .uri("/error")
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
                .expectBody(String.class)
                .value(s -> assertThat(s).isEqualTo("{\"errors\":{\"Username\":[\"already in use\"]}}"));
    }

    @Test
    void shouldFormatValidationError() {
        var body = new TestDTO()
                .setEmail("not a email")
                .setName("");
        client.post()
                .uri("/validation")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isEqualTo(UNPROCESSABLE_ENTITY)
                .expectBody(String.class)
                .value((s) -> assertThat(s)
                        .contains("{\"errors\":{\"name\":[\"must not be empty\"]")
                        .contains("\"email\":")
                        .contains("must be a well-formed email address")
                        .contains("length must be between 15 and 2147483647"));
    }

    @RestController
    static class Controller {
        @GetMapping("/error")
        void error() {
            throw new InvalidRequestException("Username", "already in use");
        }

        @PostMapping("/validation")
        void validation(@Valid @RequestBody TestDTO testDTO) {
        }
    }

    static class TestDTO {
        @NotEmpty
        String name;

        @Email
        @Length(min = 15)
        String email;

        public String getName() {
            return name;
        }

        public TestDTO setName(String name) {
            this.name = name;
            return this;
        }

        public String getEmail() {
            return email;
        }

        public TestDTO setEmail(String email) {
            this.email = email;
            return this;
        }
    }
}
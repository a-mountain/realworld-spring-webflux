package com.realworld.springmongo.user;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class User {

    @EqualsAndHashCode.Include
    @Getter
    private final String id;
    @Getter
    @Setter
    List<String> followeeIds;
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String encodedPassword;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String bio;
    @Getter
    @Setter
    private String image;
}

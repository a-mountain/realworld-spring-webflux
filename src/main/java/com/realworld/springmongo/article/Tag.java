package com.realworld.springmongo.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@Data
@AllArgsConstructor
public class Tag {
    @Id
    private String id;

    @Indexed(unique = true)
    private final String tagName;

    public static Tag of(String tag) {
        return new Tag(null, tag);
    }
}

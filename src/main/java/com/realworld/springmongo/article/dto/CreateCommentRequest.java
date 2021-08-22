package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.article.Comment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {
    String body;

    public Comment toComment(String id, String authorId) {
        return Comment.builder()
                .id(id)
                .authorId(authorId)
                .body(body)
                .build();
    }
}

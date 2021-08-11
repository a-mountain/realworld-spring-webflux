package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.article.Comment;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentView {
    String id;
    Instant createdAt;
    Instant updatedAt;
    String body;
    ProfileView author;

    public static CommentView toCommentView(Comment comment, ProfileView author) {
        return new CommentView()
                .setId(comment.getId())
                .setCreatedAt(comment.getCreatedAt())
                .setUpdatedAt(comment.getUpdatedAt())
                .setBody(comment.getBody())
                .setAuthor(author);
    }
}

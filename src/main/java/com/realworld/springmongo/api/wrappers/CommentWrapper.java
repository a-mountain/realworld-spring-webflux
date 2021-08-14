package com.realworld.springmongo.api.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realworld.springmongo.article.dto.CommentView;
import com.realworld.springmongo.article.dto.CreateCommentRequest;
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
public class CommentWrapper<T> {
    @JsonProperty("comment")
    T content;


    @NoArgsConstructor
    public static class CreateCommentRequestWrapper extends CommentWrapper<CreateCommentRequest> {
        public CreateCommentRequestWrapper(CreateCommentRequest comment) {
            super(comment);
        }
    }

    @NoArgsConstructor
    public static class CommentViewWrapper extends CommentWrapper<CommentView> {
        public CommentViewWrapper(CommentView comment) {
            super(comment);
        }
    }
}

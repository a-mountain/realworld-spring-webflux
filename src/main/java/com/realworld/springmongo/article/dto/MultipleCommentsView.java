package com.realworld.springmongo.article.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleCommentsView {
    List<CommentView> comments = Collections.emptyList();

    public static MultipleCommentsView of(List<CommentView> comments) {
        return new MultipleCommentsView()
                .setComments(comments);
    }
}

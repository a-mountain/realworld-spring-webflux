package com.realworld.springmongo.article;

import com.realworld.springmongo.user.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindArticlesRequest {
    int limit = 0;
    int offset = 20;
    String authorId = null;
    String tag = null;
    User favoritedBy = null;
}

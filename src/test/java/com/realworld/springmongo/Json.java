package com.realworld.springmongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.MultipleArticlesView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.List;

@JsonTest
public class Json {

    @Autowired
    ObjectMapper mapper;

    @Test
    void name() throws JsonProcessingException {
        var article = new ArticleView();
        var multi = MultipleArticlesView.of(List.of(article));
        var s = mapper.writeValueAsString(multi);
        System.out.println("s = " + s);
        var obj = mapper.readValue(s, MultipleArticlesView.class);
        System.out.println("obj = " + obj);
    }
}

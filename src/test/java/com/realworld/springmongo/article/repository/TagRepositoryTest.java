package com.realworld.springmongo.article.repository;

import com.realworld.springmongo.article.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TagRepositoryTest {

    @Autowired
    TagRepository tagRepository;

    @Test
    void name() {
        var tags = List.of("tag1", "tag1", "tag2", "tag2", "tag3");
        var expectedTags = Set.of("tag1", "tag2", "tag3");
        var returnedTags = tagRepository.saveAllTags(tags).map(Tag::getTagName).collectList().block();
        var allTags = tagRepository.findAll().map(Tag::getTagName).collectList().block();
        assertThat(new HashSet<>(returnedTags)).isEqualTo(expectedTags);
        assertThat(new HashSet<>(allTags)).isEqualTo(expectedTags);
    }
}
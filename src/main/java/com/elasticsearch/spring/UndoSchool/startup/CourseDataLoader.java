package com.elasticsearch.spring.UndoSchool.startup;

import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import com.elasticsearch.spring.UndoSchool.repositories.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
public class CourseDataLoader {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public CourseDataLoader(CourseRepository repo, ObjectMapper mapper) {
        this.courseRepository = repo;
        this.objectMapper = mapper;
    }

    @PostConstruct
    public void loadCourses() throws IOException {
        InputStream is = getClass().getResourceAsStream("/sample-courses.json");

        if (is == null) {
            throw new FileNotFoundException("sample-courses.json not found in resources!");
        }

        CourseDocument[] courses = objectMapper.readValue(is, CourseDocument[].class);


        courseRepository.saveAll(Arrays.asList(courses));
    }
}

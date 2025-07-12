package com.elasticsearch.spring.UndoSchool.repositories;

import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;

import java.io.IOException;
import java.util.List;

public interface CourseRepository {
    void saveAll(List<CourseDocument> courses) throws IOException;
}

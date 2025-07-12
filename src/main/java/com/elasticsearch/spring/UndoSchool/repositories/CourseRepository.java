package com.elasticsearch.spring.UndoSchool.repositories;

import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument,String> {
}

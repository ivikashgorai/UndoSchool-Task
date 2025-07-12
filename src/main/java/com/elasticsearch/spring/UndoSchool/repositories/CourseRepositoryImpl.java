package com.elasticsearch.spring.UndoSchool.repositories;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CourseRepositoryImpl implements CourseRepository {

    private final ElasticsearchClient elasticsearchClient;

    public CourseRepositoryImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public void saveAll(List<CourseDocument> courses) throws IOException {
        var operations = courses.stream()
                .map(course -> BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index("courses")
                                .id(course.getId())
                                .document(course)
                        )))
                .collect(Collectors.toList());

        var response = elasticsearchClient.bulk(b -> b.index("courses").operations(operations));

        for (BulkResponseItem item : response.items()) {
            if (item.error() != null) {
                System.err.println("Failed to index document: " + item.error().reason());
            }
        }
    }
}

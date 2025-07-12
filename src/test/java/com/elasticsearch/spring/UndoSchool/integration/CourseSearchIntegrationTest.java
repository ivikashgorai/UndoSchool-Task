package com.elasticsearch.spring.UndoSchool.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
public class CourseSearchIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.13.4")
            .withEnv("xpack.security.enabled", "false");

    private static ElasticsearchClient elasticsearchClient;

    @BeforeAll
    static void setupClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort())
        ).build();

        elasticsearchClient = new ElasticsearchClient(
                new RestClientTransport(restClient, new JacksonJsonpMapper())
        );
    }

    @Test
    void testIndexAndSearchCourse() throws IOException {
        CourseDocument sample = new CourseDocument(
                "test-001",
                "Test Course",
                "Testing Elasticsearch integration",
                "Science",
                "COURSE",
                "6th–8th",
                10,
                14,
                120.0,
                "2025-09-01T00:00:00Z",
                null
        );

        var operation = BulkOperation.of(op -> op
                .index(idx -> idx.index("courses").id(sample.getId()).document(sample)));

        elasticsearchClient.bulk(b -> b.index("courses").operations(List.of(operation)));

        elasticsearchClient.indices().refresh(r -> r.index("courses"));

        SearchRequest request = SearchRequest.of(s -> s
                .index("courses")
                .query(q -> q.match(m -> m.field("title").query("Test Course"))));

        SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);
        assertEquals(1, response.hits().total().value());
        assertEquals("Test Course", response.hits().hits().get(0).source().getTitle());
    }
}

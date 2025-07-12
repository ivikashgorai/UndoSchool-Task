package com.elasticsearch.spring.UndoSchool.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.elasticsearch.spring.UndoSchool.dto.CourseFilterParams;
import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public CourseSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<CourseDocument> searchCourses(@Nullable String query, CourseFilterParams filters) throws IOException {
        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();

        if (query != null && !query.isEmpty()) {

            must.add(QueryBuilders.bool(b -> b
                    .should(QueryBuilders.match(m -> m
                            .field("title")
                            .query(query)
                            .fuzziness("AUTO")
                    ))

            ));

            must.add(QueryBuilders.bool(b -> b
                    .should(QueryBuilders.match(m -> m
                            .field("description")
                            .query(query)
                            .fuzziness("AUTO")
                    ))

            ));
        }

        if (filters.getCategory() != null) {
            filter.add(QueryBuilders.term(t -> t
                    .field("category")
                    .value(filters.getCategory())
            ));
        }

        if (filters.getType() != null) {
            filter.add(QueryBuilders.term(t -> t
                    .field("type")
                    .value(filters.getType())
            ));
        }

        if (filters.getMinAge() != null && filters.getMaxAge() != null) {
            filter.add(QueryBuilders.range(r -> r
                    .field("minAge")
                    .gte(JsonData.of(filters.getMinAge()))
            ));
            filter.add(QueryBuilders.range(r -> r
                    .field("maxAge")
                    .lte(JsonData.of(filters.getMaxAge()))
            ));
        }

        if (filters.getMinAge() != null) {
            // course.minAge must be >= filter.minAge
            filter.add(QueryBuilders.range(r -> r
                    .field("minAge")
                    .gte(JsonData.of(filters.getMinAge()))
            ));
        }

        if (filters.getMaxAge() != null) {
            // course.maxAge must be <= filter.maxAge
            filter.add(QueryBuilders.range(r -> r
                    .field("maxAge")
                    .lte(JsonData.of(filters.getMaxAge()))
            ));
        }



        if (filters.getMinPrice() != null || filters.getMaxPrice() != null) {
            filter.add(QueryBuilders.range(r -> r
                    .field("price")
                    .gte(filters.getMinPrice() != null ? JsonData.of(filters.getMinPrice()) : null)
                    .lte(filters.getMaxPrice() != null ? JsonData.of(filters.getMaxPrice()) : null)
            ));
        }

        if (filters.getStartDate() != null) {
            filter.add(QueryBuilders.range(r -> r
                    .field("nextSessionDate")
                    .gte(JsonData.of(filters.getStartDate()))
            ));
        }

        BoolQuery boolQuery = QueryBuilders.bool()
                .must(must)
                .filter(filter)
                .build();

        String sortField = switch (filters.getSort()) {
            case "priceAsc", "priceDesc" -> "price";
            default -> "nextSessionDate";
        };

        SortOrder sortOrder = switch (filters.getSort()) {
            case "priceDesc" -> SortOrder.Desc;
            default -> SortOrder.Asc;
        };

        int from = filters.getPage() * filters.getSize();

        SearchRequest request = SearchRequest.of(s -> s
                .index("courses")
                .query(q -> q.bool(boolQuery))
                .from(from)
                .size(filters.getSize())
                .sort(so -> so.field(f -> f.field(sortField).order(sortOrder)))
        );

        SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);
        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }


    public List<String> suggestCourseTitles(String prefix) throws IOException {
        var response = elasticsearchClient.search(s -> s
                        .index("courses")
                        .suggest(sg -> sg
                                .suggesters("course-suggest", s1 -> s1
                                        .prefix(prefix)
                                        .completion(c -> c
                                                .field("suggest")
                                                .skipDuplicates(true)
                                                .size(10)
                                        )
                                )
                        ),
                Void.class
        );

        return response.suggest()
                .get("course-suggest")
                .stream()
                .flatMap(suggestion -> suggestion.completion().options().stream())
                .map(option -> option.text())
                .toList();
    }

}

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
import java.time.Instant;
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

        // Full-text search on title and description
        if (query != null && !query.isEmpty()) {
            must.add(QueryBuilders.multiMatch(m -> m
                    .fields("title", "description")
                    .query(query)));
        }

        // Filters
        if (filters.getCategory() != null) {
            filter.add(QueryBuilders.term(t -> t
                    .field("category.keyword")
                    .value(filters.getCategory())));
        }

        if (filters.getType() != null) {
            filter.add(QueryBuilders.term(t -> t
                    .field("type.keyword")
                    .value(filters.getType())));
        }

        if (filters.getMinAge() != null || filters.getMaxAge() != null) {
            filter.add(QueryBuilders.range(r -> r
                    .field("minAge")
                    .gte(filters.getMinAge() != null ? JsonData.of(filters.getMinAge()) : null)
                    .lte(filters.getMaxAge() != null ? JsonData.of(filters.getMaxAge()) : null)
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


        // Build request
        String sortFieldValue;
        SortOrder sortOrderValue;

        if ("priceAsc".equals(filters.getSort())) {
            sortFieldValue = "price";
            sortOrderValue = SortOrder.Asc;
        } else if ("priceDesc".equals(filters.getSort())) {
            sortFieldValue = "price";
            sortOrderValue = SortOrder.Desc;
        } else {
            sortFieldValue = "nextSessionDate";
            sortOrderValue = SortOrder.Asc;
        }

// Now these are effectively final
        SearchRequest request = SearchRequest.of(s -> s
                .index("courses")
                .query(q -> q.bool(boolQuery))
                .from(filters.getPage() * filters.getSize())
                .size(filters.getSize())
                .sort(so -> so.field(f -> f.field(sortFieldValue).order(sortOrderValue)))
        );

        SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);
        return response.hits().hits().stream().map(Hit::source).toList();
    }
}

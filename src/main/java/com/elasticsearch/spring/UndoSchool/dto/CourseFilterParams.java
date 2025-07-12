package com.elasticsearch.spring.UndoSchool.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CourseFilterParams {
    private String category;
    private String type;
    private Integer minAge;
    private Integer maxAge;
    private Double minPrice;
    private Double maxPrice;
    private String sort = "upcoming";
    private String startDate;
    private int page = 0;
    private int size = 50;
}


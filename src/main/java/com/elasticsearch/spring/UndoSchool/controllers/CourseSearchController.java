package com.elasticsearch.spring.UndoSchool.controllers;

import com.elasticsearch.spring.UndoSchool.dto.CourseFilterParams;
import com.elasticsearch.spring.UndoSchool.entity.CourseDocument;
import com.elasticsearch.spring.UndoSchool.service.CourseSearchService;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class CourseSearchController {
    private Logger logger = LoggerFactory.getLogger(CourseSearchController.class);

    private final CourseSearchService service;

    public CourseSearchController(CourseSearchService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> searchCourses(CourseFilterParams filters, @RequestParam(required = false) String q) throws IOException {
        List<CourseDocument> results = service.searchCourses(q, filters);
        logger.info(q);
        Map<String, Object> response = new HashMap<>();
        response.put("total", results.size());
        response.put("courses", results);
        return response;
    }

    @GetMapping("/suggest")
    public List<String> suggestCourses(@RequestParam String q) throws IOException {
        return service.suggestCourseTitles(q);
    }

}


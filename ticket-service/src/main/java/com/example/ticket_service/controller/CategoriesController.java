package com.example.ticket_service.controller;

import com.example.ticket_service.dto.response.CategoriesResponse;
import com.example.ticket_service.service.CategoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoriesResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}

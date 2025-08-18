package com.example.ticket_service.service;

import com.example.ticket_service.dto.response.CategoriesResponse;

import java.util.List;

public interface CategoriesService {
    List<CategoriesResponse> getAllCategories();
}

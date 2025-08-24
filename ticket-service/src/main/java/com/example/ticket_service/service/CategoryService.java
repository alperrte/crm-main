// service/CategoryService.java
package com.example.ticket_service.service;

import com.example.ticket_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> listActive();
}

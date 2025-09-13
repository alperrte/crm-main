package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.response.CategoryResponse;
import com.example.ticket_service.repository.CategoryRepository;
import com.example.ticket_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .key(c.getKey())
                        .name(c.getDisplayName())
                        .build())
                .toList();
    }
}

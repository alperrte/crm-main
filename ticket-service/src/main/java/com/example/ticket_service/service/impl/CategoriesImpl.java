package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.response.CategoriesResponse;
import com.example.ticket_service.entity.CategoriesEntity;
import com.example.ticket_service.repository.CategoriesRepository;
import com.example.ticket_service.service.CategoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriesImpl implements CategoriesService {

    private final CategoriesRepository categoryRepository;

    @Override
    public List<CategoriesResponse> getAllCategories() {
        List<CategoriesEntity> categories = categoryRepository.findAll();
        return categories.stream()
                .map(c -> CategoriesResponse.builder()
                        .id(c.getId())
                        .key(c.getKey())
                        .displayName(c.getDisplayName())
                        .targetId(c.getTargetId())
                        .active(c.getActive())
                        .created(c.getCreated())
                        .updated(c.getUpdated())
                        .build())
                .collect(Collectors.toList());
    }
}

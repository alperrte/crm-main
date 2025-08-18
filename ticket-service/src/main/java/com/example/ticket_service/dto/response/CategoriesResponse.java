package com.example.ticket_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoriesResponse {
    private Long id;
    private String key;
    private String displayName;
    private int targetId;
    private Boolean active;
    private LocalDateTime created;
    private LocalDateTime updated;
}

package com.example.ticket_service.dto.request;

import lombok.Data;

@Data
public class CategoriesRequest {
    private String key;
    private int targetId;
    private String displayName;
}

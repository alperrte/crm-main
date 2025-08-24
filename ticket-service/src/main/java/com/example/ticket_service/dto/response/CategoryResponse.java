// dto/response/CategoryResponse.java
package com.example.ticket_service.dto.response;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Integer id,
        String key,
        String name
) {}

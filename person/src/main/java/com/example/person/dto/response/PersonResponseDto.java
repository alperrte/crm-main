package com.example.person.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonResponseDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private Boolean active;
    private Long departmentId;
}

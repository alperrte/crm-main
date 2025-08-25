package com.example.person.dto.request;

import lombok.Data;

@Data
public class PersonRequestDto {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private Long departmentId; // ✅ NULL gelebilir
}

package com.example.user_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class DepartmentClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${department.service.url}")
    private String departmentServiceUrl;   // ör: http://department-service:8081

    // ✅ Departman ID ile departman getir
    public Map<String, Object> getDepartmentById(Long departmentId, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    departmentServiceUrl + "/departments/" + departmentId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.warn("⚠️ Department {} bulunamadı, status={}", departmentId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ DepartmentService çağrısı (getById) hata verdi, id=" + departmentId, e);
        }
        return null;
    }
}

package com.example.user_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PersonClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${person.service.url}")
    private String personServiceUrl;   // ör: http://person-service:8082

    // ✅ Var olan Person getir
    public Map<String, Object> getPersonById(Long personId, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    personServiceUrl + "/api/persons/" + personId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.warn("⚠️ Person {} bulunamadı, status={}", personId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ PersonService çağrısı (getById) hata verdi, id=" + personId, e);
        }
        return null;
    }

    // ✅ Yeni Person oluştur (opsiyonel kullanım)
    public Long createPerson(String name, String surname, String email, String phone,
                             Long departmentId, String jwtToken) {
        Map<String, Object> req = new HashMap<>();
        req.put("name", name != null ? name : "");
        req.put("surname", surname != null ? surname : "");
        req.put("email", email != null ? email : "");
        req.put("phone", phone != null ? phone : "");
        req.put("departmentId", departmentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    personServiceUrl + "/api/persons",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object idObj = response.getBody().get("id");
                if (idObj != null) {
                    return Long.valueOf(idObj.toString());
                }
            }
        } catch (Exception e) {
            log.error("❌ PersonService çağrısı (create) hata verdi", e);
        }

        return null;
    }

    // ✅ Person sil
    public void deletePerson(Long personId, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    personServiceUrl + "/api/persons/" + personId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Person {} başarıyla silindi", personId);
            } else {
                log.warn("⚠️ Person {} silinemedi, status={}", personId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ PersonService çağrısı (delete) hata verdi, id=" + personId, e);
        }
    }
}

package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StatClient {

    private final RestTemplate rest;
    private final String statsServiceUri;

    public StatClient(@Value("${statistics-server.url:http://localhost:9090}") String statsServiceUri,
                      RestTemplate rest) {
        this.rest = rest;
        this.statsServiceUri = statsServiceUri;
    }

    public void addHit(HitDto hitDto) {
        String url = statsServiceUri + "/hit";
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(hitDto);

        log.info("POST-запрос отправлен по {}", url);
        try {
            rest.exchange(url, HttpMethod.POST, requestEntity, Object.class);
            log.info("Выполнено");
        } catch (Exception e) {
            log.error("Ошибка при отправке запроса на {}", url, e);
        }
    }

    public ResponseEntity<Object> getStats(String start, String end, String[] uris, boolean unique) {
        StringBuilder pathBuilder = new StringBuilder(statsServiceUri + "/stats?start={start}&end={end}&unique={unique}");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("unique", unique);

        if (uris != null && uris.length > 0) {
            String joinedUris = String.join(",", uris);
            parameters.put("uris", joinedUris);
            pathBuilder.append("&uris={uris}");
        }

        String path = pathBuilder.toString();
        log.info("GET-запрос отправлен по {}", path);

        try {
            ResponseEntity<Object> response = rest.getForEntity(path, Object.class, parameters);
            log.info("Ответ: status={}, body={}", response.getStatusCode(), response.getBody());
            return response;
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса на {}", path, e);
            return ResponseEntity.status(500).body("Ошибка при обращении к серверу");
        }
    }
}

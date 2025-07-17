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

@Slf4j
@Component
public class StatClient {
    private final RestTemplate restTemplate;
    private final String statsServiceUri;

    public StatClient(@Value("${stat-server.url:http://localhost:9090}") String statsServiceUri) {
        this.statsServiceUri = statsServiceUri;
        this.restTemplate = new RestTemplate();
    }

    public void addHit(HitDto hitDto) {
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(hitDto);
        log.info("Отправка POST-запроса в StatServer: {}", hitDto);
        try {
            restTemplate.exchange(statsServiceUri + "/hit", HttpMethod.POST, requestEntity, Void.class);
            log.info("POST-запрос успешно выполнен.");
        } catch (Exception e) {
            log.error("Ошибка при отправке POST-запроса в StatServer", e);
        }
    }

    public ResponseEntity<StatDto[]> getStats(String start, String end, String[] uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("unique", unique);

        StringBuilder pathBuilder = new StringBuilder(statsServiceUri)
                .append("/stats?start={start}&end={end}&unique={unique}");

        if (uris != null && uris.length > 0) {
            parameters.put("uris", uris);
            pathBuilder.append("&uris={uris}");
        }

        String path = pathBuilder.toString();
        log.info("Отправка GET-запроса в StatServer: path={}, params={}", path, parameters);

        try {
            ResponseEntity<StatDto[]> response = restTemplate.getForEntity(path, StatDto[].class, parameters);
            log.info("Ответ от StatServer: status={}, body={}", response.getStatusCode(), response.getBody());

            return response;
        } catch (Exception e) {
            log.error("Ошибка при отправке GET-запроса в StatServer", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
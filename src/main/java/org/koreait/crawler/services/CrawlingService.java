package org.koreait.crawler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.koreait.crawler.controllers.RequestCrawling;
import org.koreait.crawler.entities.CrawledData;
import org.koreait.crawler.repositories.CrawledDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawledDataRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Value("${api.server.url}")
    private String apiUrl;

    public List<CrawledData> process(RequestCrawling form) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String body = om.writeValueAsString(form);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(URI.create(apiUrl + "/crawler"), request, String.class);
            System.out.println(response.getBody());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

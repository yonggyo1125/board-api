package org.koreait.crawler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.koreait.crawler.controllers.RequestCrawling;
import org.koreait.crawler.entities.CrawledData;
import org.koreait.crawler.repositories.CrawledDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

            ResponseEntity<List> response = restTemplate.postForEntity(URI.create(apiUrl + "/crawler"), request, List.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            List<CrawledData> items = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Object data : response.getBody()) {
                Map<String, Object> item = (Map<String, Object>)data;

                String link = (String)item.get("link");
                int hash = Objects.hash(link);
                LocalDate date = item.get("date") == null ? null : LocalDate.parse((String)item.get("date"), formatter);
                String title = (String)item.get("title");
                String content = (String)item.get("content");
                boolean html = (boolean)item.get("is_html");
                List<String> images = (List<String>) item.get("image");

                CrawledData _item = new CrawledData();
                _item.setHash(hash);
                _item.setLink(link);
                _item.setDate(date);
                _item.setTitle(title);
                _item.setContent(content);
                _item.setImage(images == null ? null : String.join(",", images));
                _item.setHtml(html);
                items.add(_item);
            }

            repository.saveAllAndFlush(items);

            return items;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 6L)
    public void scheduledJob() {
        // 주기적으로 데이터를 수입하는 로직

    }
}

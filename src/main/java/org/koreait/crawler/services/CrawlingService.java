package org.koreait.crawler.services;

import lombok.RequiredArgsConstructor;
import org.koreait.crawler.entities.CrawledData;
import org.koreait.crawler.repositories.CrawledDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawledDataRepository repository;
    private final RestTemplate restTemplate;

    @Value("${api.server.url}")
    private String apiUrl;

    public List<CrawledData> process() {
        return null;
    }
}

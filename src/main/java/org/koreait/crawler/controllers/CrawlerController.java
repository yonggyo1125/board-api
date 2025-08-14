package org.koreait.crawler.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.crawler.entities.CrawledData;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crawler")
@Tag(name="Crawler API", description = "..")
public class CrawlerController {

    private final HttpServletRequest request;


    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PATCH})
    public CrawledData update() {
        String mode = request.getMethod().equalsIgnoreCase("PATCH") ? "update" : "register";

        return null;
    }

}

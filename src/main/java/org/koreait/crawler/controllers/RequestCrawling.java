package org.koreait.crawler.controllers;

import lombok.Data;

import java.util.List;

@Data
public class RequestCrawling {
    private String url;
    private List<String> keywords;
    private String linkSelector;
    private String titleSelector;
    private String dateSelector;
    private String contentSelector;
    private String urlPrefix;
}

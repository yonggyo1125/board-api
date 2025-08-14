package org.koreait.crawler.services;

import org.junit.jupiter.api.Test;
import org.koreait.crawler.controllers.RequestCrawling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CrawlingServiceTest {

    @Autowired
    private CrawlingService service;

    @Test
    void crawlingTest() {
        RequestCrawling form = new RequestCrawling();
        form.setUrl("https://www.me.go.kr/mamo/web/index.do?menuId=631");
        form.setKeywords(List.of("환경", "수도권"));
        form.setLinkSelector(".brd_body .title a");
        form.setTitleSelector(".board_view .board_tit");
        form.setDateSelector(".board_view .createDate");
        form.setContentSelector(".board_view .board_con");
        form.setUrlPrefix("https://www.me.go.kr");

        service.process(form);
    }
}

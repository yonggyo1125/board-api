package org.koreait.crawler.repositories;

import org.koreait.crawler.entities.CrawledData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CrawledDataRepository extends JpaRepository<CrawledData, Integer>, QuerydslPredicateExecutor<CrawledData> {
}

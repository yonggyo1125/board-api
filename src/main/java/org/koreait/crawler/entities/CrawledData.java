package org.koreait.crawler.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
import org.koreait.global.entities.BaseEntity;

@Data
@Entity
public class CrawledData extends BaseEntity {
    @Id
    private Integer hash;

    private String link;
    private String title;

    @Lob
    private String content;

    @Lob
    private String image;


    private boolean html;
}

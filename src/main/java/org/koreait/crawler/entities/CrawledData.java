package org.koreait.crawler.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
import org.koreait.global.entities.BaseEntity;

import java.time.LocalDate;

@Data
@Entity
public class CrawledData extends BaseEntity {
    @Id
    private Integer hash;

    @Column(length = 500)
    private String link;
    private String title;

    @Lob
    private String content;

    @Lob
    private String image;


    private boolean html;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}

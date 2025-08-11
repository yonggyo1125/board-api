package org.koreait.board.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

@Data
@Entity
@IdClass(BoardViewId.class)
public class BoardView {
    @Id
    private Long seq; // 게시글 번호

    @Id
    private int hash; // 비회원 : IP + User-Agent / 회원 : IP + User-Agent + 회원번호
}
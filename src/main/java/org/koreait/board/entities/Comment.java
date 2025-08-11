package org.koreait.board.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.koreait.global.entities.BaseEntity;
import org.koreait.member.entities.Member;

import java.io.Serializable;

@Data
@Entity
@Table(indexes = {
        @Index(name="idx_comment_basic", columnList = "board_data_seq, createdAt")
})
public class Comment extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="board_data_seq")
    private BoardData item;

    @ManyToOne(fetch=FetchType.LAZY)
    private Member member;

    @Column(length=45, nullable = false)
    private String commenter;

    @Column(length=65)
    private String guestPw;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length=20)
    private String ip;

    private String ua;

    @Transient
    private boolean editable; // 게시글 수정, 삭제 가능 여부(버튼 노출 여부)

    @Transient
    private boolean mine; // 직접 작성한 게시글인지 여부

    @Transient
    private boolean guest; // 비회원 게시글 여부
}
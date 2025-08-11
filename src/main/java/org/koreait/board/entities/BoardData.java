package org.koreait.board.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.koreait.file.entities.FileInfo;
import org.koreait.global.entities.BaseEntity;
import org.koreait.member.entities.Member;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(indexes = {
        @Index(name="idx_board_data_basic", columnList = "bid, notice DESC, createdAt DESC"),
        @Index(name="idx_baord_data_category", columnList = "bid, category, notice DESC, createdAt DESC")
})
public class BoardData extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(length=45, nullable = false)
    private String gid;

    @JoinColumn(name="bid")
    @ManyToOne(fetch= FetchType.LAZY)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(length=60)
    private String category; // 게시글 분류

    @Column(length=60, nullable = false)
    private String poster; // 작성자

    @Column(length=65)
    private String guestPw; // 비회원 게시글 수정, 삭제를 위한 비밀번호

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String content;

    private boolean notice; // 공지글 여부
    private boolean secret; // 비밀글 여부

    private int viewCount; // 조회수

    private int commentCount; // 댓글 수

    @Column(length=20)
    private String ip; // 작성자 IP 주소

    private String ua; // User-Agent 정보, 작성자의 브라우저 정보

    private boolean plainText; // true : 에디터(HTML)를 사용하지 않은 일반 텍스트 게시글

    @Transient
    private List<FileInfo> editorImages;

    @Transient
    private List<FileInfo> attachFiles;

    @Transient
    private boolean guest;

    @Transient
    private boolean editable; // 게시글 수정, 삭제 가능 여부

    @Transient
    private boolean mine; // 내 게시글
}

package org.koreait.board.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.board.controllers.RequestComment;
import org.koreait.board.entities.BoardData;
import org.koreait.board.entities.Comment;
import org.koreait.board.entities.QComment;
import org.koreait.board.repositories.BoardDataRepository;
import org.koreait.board.repositories.CommentRepository;
import org.koreait.member.libs.MemberUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
@RequiredArgsConstructor
public class CommentUpdateService {
    private final CommentRepository commentRepository;
    private final BoardDataRepository boardDataRepository;
    private final BoardInfoService boardInfoService;
    private final HttpServletRequest request;
    private final PasswordEncoder encoder;
    private final MemberUtil memberUtil;

    public Comment process(RequestComment form) {
        Long seq = form.getSeq();
        Comment item = null;
        if (seq != null && (item = commentRepository.findById(seq).orElse(null)) != null) { // 댓글 수정

        } else { // 댓글 추가
            item = new Comment();
            item.setMember(memberUtil.getMember());
            BoardData boardData = boardInfoService.get(form.getBoardDataSeq());
            item.setItem(boardData);
            item.setIp(request.getRemoteAddr());
            item.setUa(request.getHeader("User-Agent"));
        }

        item.setCommenter(form.getCommenter());
        item.setContent(form.getContent());

        String guestPw = form.getGuestPw();
        if (StringUtils.hasText(guestPw)) {
            item.setGuestPw(encoder.encode(guestPw));
        }

        commentRepository.saveAndFlush(item);

        // 댓글 갯수 업데이트
        updateCommentCount(form.getBoardDataSeq());

        return item;
    }

    /**
     * 게시글별 댓글 갯수 업데이트
     *
     * @param boardDataSeq
     */
    public void updateCommentCount(Long boardDataSeq) {
        QComment comment = QComment.comment;
        long total = commentRepository.count(comment.item.seq.eq(boardDataSeq));

        BoardData item = boardDataRepository.findById(boardDataSeq).orElse(null);
        if (item != null) {
            item.setCommentCount((int)total);
            boardDataRepository.saveAndFlush(item);
        }
    }
}
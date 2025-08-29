package org.koreait.board.services;

import lombok.RequiredArgsConstructor;
import org.koreait.board.entities.Board;
import org.koreait.board.entities.BoardData;
import org.koreait.board.entities.Comment;
import org.koreait.board.exceptions.BoardNotFoundException;
import org.koreait.board.exceptions.GuestPasswordCheckException;
import org.koreait.board.services.configs.BoardConfigInfoService;
import org.koreait.global.exceptions.UnAuthorizedException;
import org.koreait.global.libs.Utils;
import org.koreait.member.constants.Authority;
import org.koreait.member.entities.Member;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.services.MemberSessionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
@RequiredArgsConstructor
public class BoardAuthService {
    private final Utils utils;
    private final MemberUtil memberUtil;
    private final BoardConfigInfoService configInfoService;
    private final BoardInfoService infoService;
    private final CommentInfoService commentInfoService;
    private final MemberSessionService session;

    public void check(String mode, String bid, Long seq) {
        // 관리자는 모두 가능 - 권한체크 필요 없음
        if (memberUtil.isAdmin()) {
            return;
        }

        Board board = null; // 게시판 설정
        BoardData item = null; // 게시글
        Comment comment = null; // 댓글

        // 글작성, 글목록
        if (StringUtils.hasText(bid)) {
            board = configInfoService.get(bid);
        }

        // 게시글 보기, 게시글 삭제, 게시글 수정, 댓글 수정, 댓글 삭제
        if (seq != null && seq > 0L) {
            if (mode.startsWith("comment_")) { // 댓글 수정, 삭제
                comment = commentInfoService.get(seq);
                item = comment.getItem(); // 게시글
            } else { // 게시글
                item = infoService.get(seq);
            }

            board = item.getBoard();
        }

        // 게시판 사용 여부
        if (!board.isActive()) {
            throw new BoardNotFoundException();
        }

        // 글작성, 글보기, 글 목록 권한 체크 S
        if (mode.equals("write") || mode.equals("list") || mode.equals("view")) {
            Authority authority = mode.equals("write") ? board.getWriteAuthority() : mode.equals("view") ? board.getViewAuthority() : board.getListAuthority();
            if ((authority == Authority.MEMBER && !memberUtil.isLogin()) || (authority == Authority.ADMIN && !memberUtil.isAdmin())) {
                throw new UnAuthorizedException();
            }
        }
        // 글작성, 글 목록 권한 체크 S

        /**
         * 글 수정, 글 삭제 권한 체크 S
         * 1. 비회원
         *  - 해당 게시글에 대해서 본인 확인이 되었는지 세션 값을 가지고 체크
         *      "board_seq_게시글번호"라는 키값이 존재하면 본인 확인이 된것이다.
         *      - 키값이 존재하지 않으면 비회원 비밀번호 확인 페이지로 넘어간다.
         *      - board_guest_seq 키 : 값 - 게시글 번호 : 세션에 저장
         * 2. 회원
         *      회원번호가 로그인한 사용자의 회원번호와 일치하는지
         */
        if (item != null && (mode.equals("update") || mode.equals("delete"))) {
            if (item.isGuest()) { // 비회원 게시글
                if (session.get("board_seq_" + seq) == null) { // 비회원 인증을 받지 않은 상태
                    throw new GuestPasswordCheckException(); // 비밀번호 확인 페이지 출력
                }
            } else { // 회원 게시글
                Member boardMember = item.getMember();
                if (!memberUtil.isLogin() || !boardMember.getSeq().equals(memberUtil.getMember().getSeq())) { // 직접 작성한 게시글이 아닌 경우
                    throw new UnAuthorizedException();
                }
            }
        }
        // 글 수정, 글 삭제 권한 체크 E

        // 댓글 수정, 댓글 삭제 권한 체크 S
        if (comment != null && mode.startsWith("comment_")) {
            if (comment.isGuest()) { // 비회원 댓글
                if (session.get("comment_seq_" + seq) == null) { // 비회원 인증을 받지 않은 상태
                    throw new GuestPasswordCheckException(); // 비밀번호 확인 페이지 출력
                }
            } else { // 회원 댓글
                Member commentMember = comment.getMember();
                if (!memberUtil.isLogin() || !commentMember.getSeq().equals(memberUtil.getMember().getSeq())) { // 직접 작성한 게시글이 아닌 경우
                    throw new UnAuthorizedException();
                }
            }
        }
        // 댓글 수정, 댓글 삭제 권한 체크 E
    }

    /**
     * 게시글 작성, 글 목록
     * @param mode
     * @param bid
     */
    public void check(String mode, String bid) {
        check(mode, bid, null);
    }

    /**
     * 게시글 보기, 게시글 수정
     * @param mode
     * @param seq
     */
    public void check(String mode, Long seq) {
        check(mode, null, seq);
    }
}
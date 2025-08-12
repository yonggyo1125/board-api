package org.koreait.board.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.koreait.board.entities.Board;
import org.koreait.board.entities.BoardData;
import org.koreait.board.entities.Comment;
import org.koreait.board.services.BoardAuthService;
import org.koreait.board.services.BoardInfoService;
import org.koreait.board.services.CommentInfoService;
import org.koreait.board.services.configs.BoardConfigInfoService;
import org.koreait.global.search.ListData;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name="게시판 구현 API", description = "게시글 작성, 수정, 조회, 삭제 등의 기능을 제공")
public class BoardController {
    private final BoardInfoService infoService;
    private final BoardAuthService authService;
    private final BoardConfigInfoService configInfoService;
    private final CommentInfoService commentInfoService;

    @GetMapping("/info/{seq}")
    public BoardData info(@PathVariable("seq") Long seq) {

        return infoService.get(seq);
    }

    @GetMapping({"/list/{bid}", "/search"})
    public ListData<BoardData> getList(@PathVariable(name="bid", required = false) String bid, @ModelAttribute BoardSearch search) {
        if (StringUtils.hasText(bid)) {
            search.setBid(List.of(bid));
        }

        return infoService.getList(search);
    }

    private void commonProcess(Long seq, String mode, Model model)  {
        BoardData item = null;
        if (mode.startsWith("comment_")) { // 댓글 수정, 삭제일 경우
            Comment comment = commentInfoService.get(seq);
            item = comment.getItem();
            model.addAttribute("comment", comment);
        } else {
            item = infoService.get(seq);
        }

            model.addAttribute("item", item);

            authService.check(mode, seq); // 글보기, 글수정, 댓글 수정, 댓글 삭제시 권한 체크

        Board board = item.getBoard();
        commonProcess(board.getBid(), mode, model);
    }

    private void commonProcess(String bid, String mode, Model model) {
        Board board = configInfoService.get(bid);

        authService.check(mode, bid); // 글쓰기, 글목록에서의 권한 체크

        mode = StringUtils.hasText(mode) ? mode : "list";

        model.addAttribute("board", board);
        model.addAttribute("mode", mode);
    }
}

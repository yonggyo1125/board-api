package org.koreait.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.board.entities.Board;
import org.koreait.board.entities.BoardData;
import org.koreait.board.entities.Comment;
import org.koreait.board.services.*;
import org.koreait.board.services.configs.BoardConfigInfoService;
import org.koreait.board.validators.BoardValidator;
import org.koreait.board.validators.CommentValidator;
import org.koreait.global.exceptions.BadRequestException;
import org.koreait.global.libs.Utils;
import org.koreait.global.search.ListData;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.services.MemberSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
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
    private final CommentUpdateService commentUpdateService;
    private final CommentDeleteService commentDeleteService;
    private final CommentValidator commentValidator;
    private final BoardUpdateService updateService;
    private final BoardDeleteService deleteService;
    private final BoardValidator boardValidator;
    private final MemberUtil memberUtil;
    private final HttpServletRequest request;
    private final MemberSessionService session;
    private final PasswordEncoder encoder;
    private final Utils utils;

    @Operation(summary = "게시글 한개 조회", method = "GET", description = "경로변수 형태로 게시글 조회, /api/v1/board/info/게시글번호 형식으로 조회 요청")
    @ApiResponse(responseCode = "200", description = "게시글 한개")
    @Parameter(name="seq", required = true, in= ParameterIn.PATH, description = "게시글 등록번호")
    @GetMapping("/info/{seq}")
    public BoardData info(@PathVariable("seq") Long seq, Model model) {
        commonProcess(seq, "view", model);

        return infoService.get(seq);
    }

    @Operation(summary = "게시판별 목록 조회, 여러 게시판의 통합 검색", method = "GET", description = "/api/v1/board/list/게시판 아이디 : 게시판별 목록 조회, /api/v1/board/search : 여러 게시판 통합 검색")
    @ApiResponse(responseCode = "200", description = "게시글 목록과 페이징을 위한 데이터가 함께 출력")
    @Parameter(name="bid", required = true, in = ParameterIn.PATH, description = "게시판 아이디")
    @GetMapping({"/list/{bid}", "/search"})
    public ListData<BoardData> getList(@PathVariable(name="bid", required = false) String bid, @ModelAttribute BoardSearch search, Model model) {
        commonProcess(bid, "list", model);
        
        if (StringUtils.hasText(bid)) {
            search.setBid(List.of(bid));
        }

        return infoService.getList(search);
    }

    @Operation(summary = "게시글 등록/수정 처리", method = "POST, PATCH", description = "POST로 요청을 보내면 등록 처리, PATCH로 요청을 보내면 수정 처리, 수정 처리 요청을 보낼 경우 seq(게시글 등록번호) 필수 항목")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 등록 완료"),
            @ApiResponse(responseCode = "200", description = "게시글 수정 완료")
    })
    @Parameters({
            @Parameter(name="seq", required = true, description = "게시글 수정시 필수 항목, 게시글 등록 번호"),
            @Parameter(name="bid", required = true, description = "게시판 아이디"),
            @Parameter(name="category", description = "게시글 분류"),
            @Parameter(name="poster", required = true, description = "작성자"),
            @Parameter(name="guestPw", required = true, description = "비회원 게시글인 경우 필수 항목, 비회원 게시글 수정, 삭제 비밀번호"),
            @Parameter(name="subject", required = true, description = "글 제목"),
            @Parameter(name="content", required = true, description = "글 내용"),
            @Parameter(name="notice", description = "공지글 여부, 관리자 권한만 적용 가능"),
            @Parameter(name="secret", description = "비밀글 여부")
    })
    @RequestMapping(path="/update", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<BoardData> update(@Valid @RequestBody RequestBoard form, Errors errors, Model model) {
        String mode = request.getMethod().equalsIgnoreCase("PATCH") ? "update" : "write";
        form.setMode(mode);

        HttpStatus status = mode.equals("update") ? HttpStatus.OK : HttpStatus.CREATED;

        if (mode.equals("update")) { // 수정
            commonProcess(form.getSeq(), mode, model);

            BoardData item = (BoardData)model.getAttribute("item");
            form.setGuest(item.isGuest());
            form.setGid(item.getGid());
        } else { // 등록
            commonProcess(form.getBid(), mode, model);
            form.setGuest(!memberUtil.isLogin());
        }

        boardValidator.validate(form, errors);
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        BoardData item = updateService.process(form);

        return ResponseEntity.status(status).body(item);
    }

    @Operation(summary = "게시글 한개 삭제", method="DELETE", description = "게시글 등록번호(seq)로 게시글 한개 삭제, 댓글이 존재 하면 소프트 삭제(삭제 표기 - deletedAt), 댓글이 없을때 하드 삭제(실 데이터 삭제)")
    @ApiResponse(responseCode = "200", description = "삭제가 처리되면 후속 처리를 위한 삭제된 게시글 데이터 출력")
    @Parameter(name="seq", required = true, in = ParameterIn.PATH, description = "게시글 등록번호")
    @DeleteMapping("/delete/{seq}")
    public BoardData delete(@PathVariable("seq") Long seq, Model model) {
        commonProcess(seq, "delete", model);
        return deleteService.process(seq);
    }

    @Operation(summary = "댓글 한개 조회", method="GET", description = "댓글 한개 조회, 댓글 수정시에 활용")
    @Parameter(name="seq", required = true, in = ParameterIn.PATH, description = "댓글 등록 번호")
    @GetMapping("/comment/{seq}")
    public Comment commentInfo(@PathVariable("seq") Long seq, Model model) {
        
        return commentInfoService.get(seq);
    }

    @Operation(summary = "게시글 하나에 있는 댓글 목록", method = "GET", description = "/comments/게시글 등록번호, 게시글 하나에 작성된 댓글 목록")
    @Parameter(name="seq", required = true, in = ParameterIn.PATH, description = "게시글 등록 번호")
    @GetMapping("/comments/{seq}")
    public List<Comment> comments(@PathVariable("seq") Long seq) {
        return commentInfoService.getList(seq);
    }
    
    @Operation(summary = "댓글 작성, 수정 처리", method = "POST,PATCH", description = "POST 요청 - 댓글 등록, PATCH 요청 - 댓글 수정, 댓글 수정시에는 댓글 등록번호인 seq가 필수 항목")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 등록 완료"),
            @ApiResponse(responseCode = "200", description = "댓글 수정 완료")
    })
    @Parameters({
            @Parameter(name = "boardDataSeq", required = true, description = "게시글 등록번호, 댓글과 연결될 게시글 번호"),
            @Parameter(name = "seq", required = true, description = "댓글 등록번호, 댓글 수정시 필수항목"),
            @Parameter(name="commenter", required = true, description = "댓글 작성자"),
            @Parameter(name="guestPw", required = true, description = "비회원 댓글 수정, 삭제 비밀번호, 비회원 게시글인 경우만 필수항목"),
            @Parameter(name="content", required = true, description = "댓글 내용")
    })
    @RequestMapping(path="/comment", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<Comment> commentUpdate(@Valid @RequestBody RequestComment form, Errors errors, Model model) {
        String mode = request.getMethod().equalsIgnoreCase("PATCH") ? "comment_update" : "comment_write";
        form.setMode(mode);

        HttpStatus status = mode.equals("comment_update") ? HttpStatus.OK : HttpStatus.CREATED;

        commentValidator.validate(form, errors);
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        // 댓글 작성, 수정 처리
        Comment item = commentUpdateService.process(form);

        return ResponseEntity.status(status).body(item);
    }

    @Operation(summary = "댓글 한개 삭제 처리", method = "DELETE", description = "댓글 등록번호(seq)로 댓글 한개 삭제")
    @Parameter(name="seq", required = true, in = ParameterIn.PATH, description = "댓글 등록 번호")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 완료시 삭제된 댓글 데이터를 출력")
    @DeleteMapping("/comment/{seq}")
    public Comment commentDelete(@PathVariable("seq") Long seq, Model model) {
        commonProcess(seq, "comment_delete", model);

        return commentDeleteService.process(seq);
    }

    @Operation(summary = "비회원 게시글 또는 댓글의 수정, 삭제 비밀번호 검증", method="POST")
    @ApiResponse(responseCode = "204")
    @Parameter(name="password", required = true, in = ParameterIn.QUERY, description = "비회원 비밀번호")
    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guestPasswordCheck(@Valid @RequestBody RequestPassword form, Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
        Long commentSeq = session.get("comment_guest_seq");
        Long seq = session.get("board_guest_seq");

        String guestPw = null, confirmKey = null;
        if (commentSeq != null) { // 댓글
            Comment item = commentInfoService.get(commentSeq);
            guestPw = item.getGuestPw();
            confirmKey = "comment_seq_" + commentSeq;
        } else { // 게시글
            BoardData item = infoService.get(seq);
            guestPw = item.getGuestPw();
            confirmKey = "board_seq_" + seq;
        }

        if (!encoder.matches(form.getPassword(), guestPw)) {
            throw new BadRequestException(utils.getMessage("비밀번호가_일치하지_않습니다."));
        }

        session.set(confirmKey, true); // 비회원 비밀번호 확인 완료
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

package org.koreait.board.services;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.board.controllers.BoardSearch;
import org.koreait.board.controllers.RequestBoard;
import org.koreait.board.entities.Board;
import org.koreait.board.entities.BoardData;
import org.koreait.board.entities.QBoardData;
import org.koreait.board.exceptions.BoardDataNotFoundException;
import org.koreait.board.repositories.BoardDataRepository;
import org.koreait.board.services.configs.BoardConfigInfoService;
import org.koreait.file.services.FileInfoService;
import org.koreait.global.search.ListData;
import org.koreait.global.search.Pagination;
import org.koreait.member.entities.Member;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.services.MemberSessionService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Lazy
@Service
@Transactional
@RequiredArgsConstructor
public class BoardInfoService {

    private final BoardConfigInfoService configInfoService;
    private final BoardDataRepository boardDataRepository;
    private final FileInfoService fileInfoService;
    private final HttpServletRequest request;
    private final JPAQueryFactory queryFactory;
    private final MemberUtil memberUtil;
    private final ModelMapper mapper;
    private final MemberSessionService session;

    /**
     * 게시글 1개 조회
     *
     * @param seq
     * @return
     */
    public BoardData get(Long seq) {
        QBoardData boardData = QBoardData.boardData;
        BoardData item = queryFactory.selectFrom(boardData)
                .leftJoin(boardData.board)
                .fetchJoin()
                .leftJoin(boardData.member)
                .fetchJoin()
                .where(boardData.seq.eq(seq))
                .fetchOne();
        if (item == null) {
            throw new BoardDataNotFoundException();
        }

        // 추가 정보 처리
        addInfo(item);

        return item;
    }

    /**
     * 게시글 수정시 조회
     * @param seq
     * @return
     */
    public RequestBoard getForm(Long seq) {
        BoardData item = get(seq);
        RequestBoard form = mapper.map(item, RequestBoard.class);
        form.setBid(item.getBoard().getBid());
        return form;
    }

    /**
     * 내가 쓴 게시글 목록
     *
     * @param search
     * @return
     */
    public ListData<BoardData> getMyList(BoardSearch search) {
        if (!memberUtil.isLogin()) {
            return new ListData<>();
        }

        search = Objects.requireNonNullElseGet(search, BoardSearch::new);

        Member member = memberUtil.getMember();
        search.setEmail(List.of(member.getEmail()));

        return getList(search);
    }

    /**
     * 특정 게시판의 목록 조회
     *
     * @param bid
     * @param search
     * @return
     */
    public ListData<BoardData> getList(String bid, BoardSearch search) {
        search = Objects.requireNonNullElseGet(search, BoardSearch::new);
        search.setBid(List.of(bid));

        return getList(search);
    }

    /**
     * 최신 게시글
     *
     * @param bid
     * @param limit
     * @return
     */
    public List<BoardData> getLatest(String bid, int limit) {
        BoardSearch search = new BoardSearch();
        search.setBid(List.of(bid));
        search.setLimit(limit);

        return getList(search).getItems();
    }

    public List<BoardData> getLatest(String bid) {
        return getLatest(bid, 10);
    }

    public ListData<BoardData> getList(BoardSearch search) {
        int page = Math.max(search.getPage(), 1);
        int limit = search.getLimit();
        List<String> bids = search.getBid();
        Board board = null;
        if (bids != null && bids.size() == 1) { // 게시판 아이디가 1개인 경우 게시판 설정 조회
            board = configInfoService.get(bids.getFirst());

            // 한페이지당 게시글 갯수
            limit = board.getRowsForPage();
        }

        limit = limit < 1 ? 20 : limit;
        int offset = (page - 1) * limit; // 레코드 시작 번호

        /* 검색 조건 처리 S */
        List<String> categories = search.getCategory();
        String sopt = search.getSopt();
        String skey = search.getSkey();
        LocalDate sDate = search.getSDate();
        LocalDate eDate = search.getEDate();
        List<String> emails = search.getEmail();

        BooleanBuilder andBuilder = new BooleanBuilder();
        QBoardData boardData = QBoardData.boardData;

        if (bids != null && !bids.isEmpty())  { // 게시판 아이디 조회
            andBuilder.and(boardData.board.bid.in(bids));
        }

        if (categories != null && !categories.isEmpty()) { // 게시판 분류 조회
            andBuilder.and(boardData.category.in(categories));
        }

        // 게시글 등록일 조회
        if (sDate != null) {
            andBuilder.and(boardData.createdAt.goe(sDate.atStartOfDay()));
        }

        if (eDate != null) {
            andBuilder.and(boardData.createdAt.loe(eDate.atTime(23, 59, 59)));
        }

        /**
         * 키워드 검색
         * sopt - ALL : 통합검색 (SUBJECT + CONTENT + NAME)
         *        SUBJECT : 게시글 제목
         *        CONTENT : 게시글 내용
         *        SUBJECT_CONTENT : 게시글 제목 + 내용
         *        NAME : 작성자명(poster) + 회원명(name) + 이메일(email)
         */
        sopt = StringUtils.hasText(sopt) ? sopt.toUpperCase() : "ALL";
        if (StringUtils.hasText(skey)) {
            skey = skey.trim();

            StringExpression subject = boardData.subject;
            StringExpression content = boardData.content;
            StringExpression name = boardData.poster.concat(boardData.member.name)
                    .concat(boardData.member.email);

            StringExpression fields = null;
            if (sopt.equals("SUBJECT")) {
                fields = subject;
            } else if (sopt.equals("CONTENT")) {
                fields = content;
            } else if (sopt.equals("SUBJECT_CONTENT")) {
                fields = subject.concat(content);
            } else if (sopt.equals("NAME")) {
                fields = name;
            } else { // 통합검색
                fields = subject.concat(content).concat(name);
            }

            andBuilder.and(fields.contains(skey));
        }

        // 회원 이메일로 게시글 조회
        if (emails != null && !emails.isEmpty()) {
            andBuilder.and(boardData.member.email.in(emails));
        }

        /* 검색 조건 처리 E */

        List<BoardData> items = queryFactory.selectFrom(boardData)
                .leftJoin(boardData.member)
                .fetchJoin()
                .where(andBuilder)
                .offset(offset)
                .limit(limit)
                .orderBy(boardData.notice.desc(), boardData.createdAt.desc())
                .fetch();

        int total = (int)boardDataRepository.count(andBuilder);

        // 추가 정보 처리
        items.forEach(this::addInfo);

        int range = 10;
        if (board != null) {
            range = board.getPageCount();
            range = range < 1 ? 10 : range;
        }

        Pagination pagination = new Pagination(page, total, range, limit, request);

        return new ListData<>(items, pagination);
    }

    /**
     * 추가 정보 처리
     *
     * @param item
     */
    private void addInfo(BoardData item) {
        String gid = item.getGid();

        // 첨부된 이미지 & 파일 목록
        item.setEditorImages(fileInfoService.getList(gid, "editor"));
        item.setAttachFiles(fileInfoService.getList(gid, "attach"));

        // 비회원 게시글 여부
        item.setGuest(item.getMember() == null);

        /**
         * 내 게시글 여부, 수정 가능 여부
         * 회원 게시글 : 작성한 회원번호와 로그인한 회원 번호가 일치
         * 비회원 게시글 : 비회원 비밀번호 확인이 완료된 게시글(board_seq_게시글번호)
         */
        boolean editable = true;
        if (item.isGuest()) { // 비회원 게시글
            item.setMine(session.get("board_seq_" + item.getSeq()) != null);
        } else { // 회원 게시글
            Member boardMember = item.getMember(); // 게시글을 작성한 회원
            Member member = memberUtil.getMember(); // 로그인한 회원
            item.setMine(memberUtil.isLogin() && boardMember.getSeq().equals(member.getSeq())); // 로그인한 회원 정보와 게시글 작성 회원 정보가 일치
            if (!memberUtil.isAdmin()) {
                editable = item.isMine();
            }
        }

        item.setEditable(editable);
    }
}
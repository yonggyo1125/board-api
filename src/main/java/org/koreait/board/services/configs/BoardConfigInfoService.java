package org.koreait.board.services.configs;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.board.controllers.RequestBoardConfig;
import org.koreait.board.entities.Board;
import org.koreait.board.entities.QBoard;
import org.koreait.board.exceptions.BoardNotFoundException;
import org.koreait.board.repositories.BoardRepository;
import org.koreait.global.search.CommonSearch;
import org.koreait.global.search.ListData;
import org.koreait.global.search.Pagination;
import org.koreait.member.constants.Authority;
import org.koreait.member.libs.MemberUtil;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Lazy
@Service
@RequiredArgsConstructor
public class BoardConfigInfoService {

    private final BoardRepository repository;
    private final HttpServletRequest request;
    private final MemberUtil memberUtil;
    private final ModelMapper mapper;

    /**
     * 게시판 설정 한개 조회
     *
     * @param bid
     * @return
     */
    public Board get(String bid) {
        Board item = repository.findById(bid).orElseThrow(BoardNotFoundException::new);

        addInfo(item); // 추가 정보 공통 처리

        return item;
    }

    /**
     * 게시판 설정 수정시 필요한 커맨드 객체 형태로 조회
     *
     * @param bid
     * @return
     */
    public RequestBoardConfig getForm(String bid) {
        Board board = get(bid);

        return mapper.map(board, RequestBoardConfig.class);
    }

    /**
     * 게시판 목록 조회
     *
     * @param search
     * @return
     */
    public ListData<Board> getList(CommonSearch search, boolean isAll) {
        int page = Math.max(search.getPage(), 1);
        int limit = search.getLimit();
        limit = limit < 1 ? 20 : limit;

        String sopt = search.getSopt();
        String skey = search.getSkey();

        BooleanBuilder andBuilder = new BooleanBuilder();
        QBoard board = QBoard.board;

        if (!isAll) { // 사용 가능 게시판만 보여주기
            andBuilder.and(board.active.eq(true));
        }

        // 키워드 검색 처리 S
        sopt = StringUtils.hasText(sopt) ? sopt.toUpperCase() : "ALL";
        if (StringUtils.hasText(skey)) {
            skey = skey.trim();

            StringExpression fields = null;
            if (sopt.equals("BID")) {
                fields = board.bid;
            } else if (sopt.equals("NAME")) {
                fields = board.name;
            } else { // 통합 검색 BID + NAME
                fields = board.bid.concat(board.name);
            }

            andBuilder.and(fields.contains(skey));
        }
        // 키워드 검색 처리 E

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("createdAt")));
        Page<Board> data = repository.findAll(andBuilder, pageable);
        List<Board> items = data.getContent();
        items.forEach(this::addInfo); // 추가정보 처리

        int total = (int)data.getTotalElements();
        Pagination pagination = new Pagination(page, total, 10, limit, request);

        return new ListData<>(items, pagination);
    }

    public ListData<Board> getList(CommonSearch search) {
        return getList(search, false);
    }

    /**
     * 게시판 설정에 대한 추가 정보 가공 처리
     *
     * @param item
     */
    private void addInfo(Board item) {

        boolean writable = true, listable = true, viewable = true, commentable = true;

        Authority writeAuthority = item.getWriteAuthority();
        if (writeAuthority != Authority.ALL && ((writeAuthority == Authority.MEMBER && !memberUtil.isLogin()) || (writeAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
            writable = false;
        }

        Authority listAuthority = item.getListAuthority();
        if (listAuthority != Authority.ALL && ((listAuthority == Authority.MEMBER && !memberUtil.isLogin()) || (listAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
            listable = false;
        }

        Authority viewAuthority = item.getViewAuthority();
        if (viewAuthority != Authority.ALL && ((viewAuthority == Authority.MEMBER && !memberUtil.isLogin()) || (viewAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
            viewable = false;
        }

        Authority commentAuthority = item.getCommentAuthority();
        if (commentAuthority != Authority.ALL && ((commentAuthority == Authority.MEMBER && !memberUtil.isLogin()) || (commentAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
            commentable = false;
        }

        item.setWritable(writable);
        item.setListable(listable);
        item.setCommentable(commentable);
        item.setViewable(viewable);

    }
}
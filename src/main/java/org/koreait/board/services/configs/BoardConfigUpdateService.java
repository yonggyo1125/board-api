package org.koreait.board.services.configs;

import lombok.RequiredArgsConstructor;
import org.koreait.board.controllers.RequestBoardConfig;
import org.koreait.board.entities.Board;
import org.koreait.board.repositories.BoardRepository;
import org.koreait.member.constants.Authority;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
public class BoardConfigUpdateService {
    private final ModelMapper mapper;
    private final BoardRepository boardRepository;

    public void process(RequestBoardConfig form) {

        form.setRowsForPage(form.getRowsForPage() < 0 ? 20 : form.getRowsForPage());
        form.setPageCount(form.getPageCount() < 0 ? 10 : form.getPageCount());
        form.setSkin(StringUtils.hasText(form.getSkin()) ? form.getSkin() : "default");
        form.setListAuthority(Objects.requireNonNullElse(form.getListAuthority(), Authority.ALL));
        form.setViewAuthority(Objects.requireNonNullElse(form.getViewAuthority(), Authority.ALL));
        form.setWriteAuthority(Objects.requireNonNullElse(form.getWriteAuthority(), Authority.ALL));
        form.setCommentAuthority(Objects.requireNonNullElse(form.getCommentAuthority(), Authority.ALL));

        Board item = mapper.map(form, Board.class);
        boardRepository.saveAndFlush(item);
    }
}
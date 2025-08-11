package org.koreait.board.services.configs;

import lombok.RequiredArgsConstructor;
import org.koreait.board.controllers.RequestBoardConfig;
import org.koreait.board.entities.Board;
import org.koreait.board.repositories.BoardRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class BoardConfigUpdateService {
    private final ModelMapper mapper;
    private final BoardRepository boardRepository;

    public void process(RequestBoardConfig form) {

        Board item = mapper.map(form, Board.class);
        boardRepository.saveAndFlush(item);
    }
}
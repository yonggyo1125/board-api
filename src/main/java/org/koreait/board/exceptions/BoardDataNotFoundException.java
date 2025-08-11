package org.koreait.board.exceptions;

import org.koreait.global.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;

public class BoardDataNotFoundException extends BadRequestException {
    public BoardDataNotFoundException() {
        super("NotFound.boardData");
        setErrorCode(true);
    }
}
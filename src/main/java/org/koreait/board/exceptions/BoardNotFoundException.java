package org.koreait.board.exceptions;

import org.koreait.global.exceptions.NotFoundException;

public class BoardNotFoundException extends NotFoundException {
    public BoardNotFoundException() {
        super("NotFound.board");
        setErrorCode(true);
    }
}
package org.koreait.board.exceptions;

import org.koreait.global.exceptions.BadRequestException;

public class CommentNotFoundException extends BadRequestException {
    public CommentNotFoundException() {
        super("NotFound.comment");
        setErrorCode(true);
    }
}
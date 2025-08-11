package org.koreait.board.exceptions;

import org.koreait.global.exceptions.UnAuthorizedException;

public class GuestPasswordCheckException extends UnAuthorizedException {
    public GuestPasswordCheckException() {
        super("Required.guest.password");
        setErrorCode(true);
    }
}

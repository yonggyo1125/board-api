package org.koreait.global.advices;

import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.CommonException;
import org.koreait.global.libs.Utils;
import org.koreait.global.rests.JSONError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestControllerAdvice("org.koreait")
public class CommonControllerAdvice {
    private final Utils utils;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSONError> errorHandler(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 기본 에러 코드는 500
        Object message = e.getMessage();

        if (e instanceof CommonException commonException) {
            status = commonException.getStatus();
            Map<String, List<String>> errorMessages = commonException.getErrorMessages(); // 커맨드 객체 검증 실패 메세지
            if (errorMessages != null) {
                message = errorMessages;
            } else {
                // 에러 코드로 관리되는 문구인 경우
                if (commonException.isErrorCode()) {
                    message = utils.getMessage((String)message);
                }
            }
        } else if (e instanceof AuthorizationDeniedException) {
            status = HttpStatus.UNAUTHORIZED;
            message = utils.getMessage("UnAuthorized");
        } else if (e instanceof DisabledException) { // 탈퇴한 회원인 경우
            status = HttpStatus.UNAUTHORIZED;
            message = utils.getMessage("Authentication.disabled");
        } else if (e instanceof AccountExpiredException) { // 계정 만료 회원인 경우
            status = HttpStatus.UNAUTHORIZED;
            message = utils.getMessage("Authentication.account.expired");
        } else if (e instanceof LockedException) { // 계정이 잠겨 있는 경우
            status = HttpStatus.UNAUTHORIZED;
            message = utils.getMessage("Authentication.account.locked");
        } else if (e instanceof CredentialsExpiredException) { // 비밀번호가 만료된 경우
            status = HttpStatus.UNAUTHORIZED;
            message = utils.getMessage("Authentication.credential.expired");
        }


        e.printStackTrace();


        return ResponseEntity.status(status).body(new JSONError(status, message));
    }
}

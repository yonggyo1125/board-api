package org.koreait.global.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CommonException extends RuntimeException {
    private final HttpStatus status;
    private Map<String, List<String>> errorMessages;
    private boolean errorCode;

    public CommonException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CommonException(Map<String, List<String>> errorMessages, HttpStatus status) {
        this.errorMessages = errorMessages;
        this.status = status;
    }
}

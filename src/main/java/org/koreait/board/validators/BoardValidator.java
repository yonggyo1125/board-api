package org.koreait.board.validators;

import org.koreait.board.controllers.RequestBoard;
import org.koreait.global.validators.PasswordValidator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Lazy
@Component
public class BoardValidator implements Validator, PasswordValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestBoard.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        /**
         * 1. 글 수정인 경우는 seq가 필수
         * 2. 비회원 게시글인 경우 비회원 비밀번호가 필수
         *      - 비밀번호 복잡성도 체크
         */
        RequestBoard form = (RequestBoard) target;
        String mode = Objects.requireNonNullElse(form.getMode(), "write");
        if (mode.equals("update") && (form.getSeq() == null || form.getSeq() < 1L)) {
            errors.rejectValue("seq", "NotNull");
        }

        // 2. 비회원 게시글인 경우 비회원 비밀번호가 필수
        if (form.isGuest()) {
            String guestPw = form.getGuestPw();
            if (!StringUtils.hasText(guestPw)) {
                errors.rejectValue("guestPw", "NotBlank");
            } else {
                if (guestPw.length() < 4) {
                    errors.rejectValue("guestPw", "Size");
                }

                if (!checkAlpha(guestPw, true) || !checkNumber(guestPw)) { // 비밀번호 복잡성도 체크
                    errors.rejectValue("Complexity", "guestPw");
                }
            }
        }
    }
}
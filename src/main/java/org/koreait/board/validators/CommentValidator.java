package org.koreait.board.validators;

import org.koreait.board.controllers.RequestComment;
import org.koreait.global.validators.PasswordValidator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Lazy
@Component
public class CommentValidator implements Validator, PasswordValidator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestComment.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        RequestComment form = (RequestComment) target;
        String mode = form.getMode();
        mode = StringUtils.hasText(mode) ? mode : "comment_write";


        String guestPw = form.getGuestPw();
        if (form.isGuest()) {
            // 비회원 비밀번호 필수 여부
            if (!StringUtils.hasText(guestPw)) {
                errors.rejectValue("guestPw", "NotBlank");
            } else {
                // 비밀번호 복잡성 체크
                if (guestPw.length() < 4) {
                    errors.rejectValue("guestPw", "Size");
                }

                if (!checkAlpha(guestPw, true) || !checkNumber(guestPw)) {
                    errors.rejectValue("guestPw", "Complexity");
                }
            }
        }

        // 댓글 수정인 경우, 댓글 등록번호인 seq가 필수
        if (mode.equals("comment_update") && form.getSeq() == null) {
            errors.rejectValue("seq", "NotNull");
        }
    }
}
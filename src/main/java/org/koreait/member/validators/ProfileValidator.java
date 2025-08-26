package org.koreait.member.validators;

import org.koreait.global.validators.MobileValidator;
import org.koreait.global.validators.PasswordValidator;
import org.koreait.member.controllers.RequestProfile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ProfileValidator implements Validator, PasswordValidator, MobileValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestProfile.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        /**
         * password, confirmPassword는
         * password 값이 있는 경우  - confirmPassword 필수 항목
         *                         - 비밀번호 자리수, 복잡성 체크
         *                         - password, confirmPassword 일치 여부 체크
         */

        RequestProfile form = (RequestProfile) target;
        String password = form.getPassword();
        String confirmPassword = form.getConfirmPassword();
        if (StringUtils.hasText(password)) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotBlank");

            if (errors.hasErrors()) return;

            // 1. 비밀번호 자리수
            if (password.length() < 8) {
                errors.rejectValue("password", "Size");
            }

            // 2. 비밀번호 복잡성
            if (!checkAlpha(password, false) || !checkNumber(password) || !checkSpecialChars(password)) {
                errors.rejectValue("password", "Complexity");
            }

            // 3. 비밀번호 확인 일치 여부
            if (!password.equals(confirmPassword)) {
                errors.rejectValue("confirmPassword", "Mismatch");
            }
        }

        // 4. 휴대전화번호 형식 검증
        String mobile = form.getMobile();
        if (!checkMobile(mobile)) {
            errors.rejectValue("mobile", "Mobile");
        }
    }
}

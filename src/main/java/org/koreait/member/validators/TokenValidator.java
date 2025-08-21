package org.koreait.member.validators;

import lombok.RequiredArgsConstructor;
import org.koreait.member.controllers.RequestToken;
import org.koreait.member.entities.Member;
import org.koreait.member.repositories.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class TokenValidator implements Validator {

    private final MemberRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestToken.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        RequestToken form = (RequestToken) target;
        if (form.isSocial()) { // 소셜 로그인 요청인 경우
            if (form.getSocialChannel() == null) {
                errors.rejectValue("socialChannel", "NotNull");
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "socialToken", "NotBlank");

        } else { // 일반 로그인 요청인 경우
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotBlank");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotBlank");

            if (errors.hasErrors()) return;

            Member member = repository.findByEmail(form.getEmail()).orElse(null);

            if (member == null) {
                errors.reject("NotFound.member.or.password");
            }

            // 비밀번호 검증

            if (member != null && !encoder.matches(form.getPassword(), member.getPassword())) {
                errors.reject("NotFound.member.or.password");

            }
        }
    }
}

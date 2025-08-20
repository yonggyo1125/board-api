package org.koreait.member.validators;

import lombok.RequiredArgsConstructor;
import org.koreait.member.controllers.RequestLoginToken;
import org.koreait.member.controllers.RequestToken;
import org.koreait.member.entities.Member;
import org.koreait.member.repositories.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
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
        if (errors.hasErrors()) {
            return;
        }


        RequestLoginToken form = (RequestLoginToken) target;
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

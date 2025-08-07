package org.koreait.member.validators;

import lombok.RequiredArgsConstructor;
import org.koreait.member.controllers.RequestToken;
import org.koreait.member.repositories.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class TokenValidator implements Validator {

    private final MemberRepository repository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestToken.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        RequestToken form = (RequestToken) target;
        if (!repository.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "NotFound.member");
        }
    }
}

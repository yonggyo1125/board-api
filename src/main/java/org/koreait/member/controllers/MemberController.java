package org.koreait.member.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.BadRequestException;
import org.koreait.global.libs.Utils;
import org.koreait.member.services.JoinService;
import org.koreait.member.validators.JoinValidator;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final JoinValidator joinValidator;
    private final JoinService joinService;
    private final Utils utils;

    // 회원 가입
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201로 응답
    public void join(@Valid @RequestBody RequestJoin form, Errors errors) {

        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        joinService.process(form);
    }

}

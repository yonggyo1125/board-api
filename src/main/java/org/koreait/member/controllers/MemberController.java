package org.koreait.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.BadRequestException;
import org.koreait.global.libs.Utils;
import org.koreait.member.entities.Member;
import org.koreait.member.jwt.TokenService;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.services.JoinService;
import org.koreait.member.validators.JoinValidator;
import org.koreait.member.validators.TokenValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Tag(name="회원 API", description = "회원 가입, 회원 인증 토큰 발급 기능 제공")
public class MemberController {
    private final JoinValidator joinValidator;
    private final JoinService joinService;
    private final TokenValidator tokenValidator;
    private final TokenService tokenService;
    private final MemberUtil memberUtil;
    private final Utils utils;

    @Operation(summary = "회원가입처리", method = "POST")
    @ApiResponse(responseCode = "201", description = "회원가입 성공시 201로 응답, 검증 실패시 400")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201로 응답
    public void join(@Valid @RequestBody RequestJoin form, Errors errors) {

        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        joinService.process(form);
    }

    /**
     * 회원 계정(이메일, 비밀번호)으로 JWT 토큰 발급
     *
     * @return
     */
    @PostMapping("/token")
    public String token(@Valid @RequestBody RequestToken form, Errors errors) {

        tokenValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        return tokenService.create(form.getEmail());
    }


    /**
     * 로그인한 회원 정보 출력
     * 
     * @return
     */
    @Operation(summary = "로그인 상태인 회원 정보를 조회", method = "GET")
    @ApiResponse(responseCode = "200")
    @GetMapping // GET /api/v1/member
    @PreAuthorize("isAuthenticated()")
    public Member myInfo() {
        return memberUtil.getMember();
    }
}

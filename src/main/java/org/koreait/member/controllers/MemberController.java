package org.koreait.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.http.ResponseEntity;
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
    private final HttpServletRequest request;
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
    @Operation(summary = "회원 인증 처리", description = "이메일과 비밀번호로 인증한 후 회원 전용 요청을 보낼수 있는 토큰(JWT)을 발급")
    @Parameters({
            @Parameter(name="email", required = true, description = "이메일, 일반 로그인 시 필수"),
            @Parameter(name="password", required = true, description = "비밀번호, 일반 로그인시 필수"),
            @Parameter(name="socialChannel", required = true, description = "소셜 로그인 채널 구분, 소셜 로그인 시 필수"),
            @Parameter(name="socialToken", required = true, description = "소셜 로그인시 발급받은 회원 구분 값, 소셜 로그인시에만 필수")
    })
    @ApiResponse(responseCode = "200", description = "인증 성공시 토큰(JWT)발급")
    @PostMapping({"/token", "/social/token"})
    public String token(@Valid @RequestBody RequestToken form, Errors errors) {
        form.setSocial(request.getRequestURI().contains("/social"));

        tokenValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        return form.isSocial() ? tokenService.create(form.getSocialChannel(), form.getSocialToken()) : tokenService.create(form.getEmail());
    }


    /**
     * 로그인한 회원 정보 출력
     * 
     * @return
     */
    @Operation(summary = "로그인 상태인 회원 정보를 조회", method = "GET")
    @ApiResponse(responseCode = "200")
    @GetMapping // GET /api/v1/member
    public ResponseEntity<Member> myInfo() {

        return memberUtil.isLogin() ? ResponseEntity.ok(memberUtil.getMember()): ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

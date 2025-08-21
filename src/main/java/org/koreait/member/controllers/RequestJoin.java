package org.koreait.member.controllers;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.koreait.member.constants.SocialChannel;

@Data
public class RequestJoin {

    private String gid;

    @NotBlank @Email
    private String email;

    private String password;
    private String confirmPassword;

    private SocialChannel socialChannel;
    private String socialToken;

    @NotBlank
    private String name;

    @NotBlank
    private String mobile;

    @AssertTrue
    private boolean termsAgree;
}

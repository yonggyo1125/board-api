package org.koreait.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.koreait.member.constants.SocialChannel;

@Data
public class RequestSocialToken extends RequestToken {
    @NotBlank
    private SocialChannel channel;

    @NotBlank
    private String token;
}

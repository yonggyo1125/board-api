package org.koreait.member.controllers;

import lombok.Data;
import org.koreait.member.constants.SocialChannel;

@Data
public class RequestToken {
    private boolean social;
    private String email;
    private String password;
    private SocialChannel socialChannel;
    private String socialToken;
}

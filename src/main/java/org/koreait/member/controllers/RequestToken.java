package org.koreait.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestToken {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}

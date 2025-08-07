package org.koreait.member.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.koreait.member.controllers.RequestJoin;
import org.koreait.member.services.JoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"default", "test"})
public class TokenServiceTest {

    @Autowired
    private JoinService joinService;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void init() {
        RequestJoin form = new RequestJoin();
        form.setEmail("user01@test.org");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setMobile("01010001000");
        form.setName("사용자01");
        form.setTermsAgree(true);

        joinService.process(form);
    }

    @Test
    @DisplayName("토큰 발급 테스트")
    void jwtCreationTest() {
        String token = tokenService.create("user01@test.org");
        System.out.println(token);

        assertNotNull(token);
    }
}

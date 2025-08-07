package org.koreait.member.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.koreait.member.services.JoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"default", "test"})
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JoinService joinService;

    @Test
    @DisplayName("회원가입 컨트롤러 테스트")
    void joinTest() throws Exception {

        RequestJoin form = new RequestJoin();
        form.setEmail("user01@test.org");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setMobile("01010001000");
        form.setName("사용자01");
        form.setTermsAgree(true);

        String body = om.writeValueAsString(form);

        mockMvc.perform(post("/api/v1/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("이메일, 비밀번호로 토큰 발급 테스트")
    void tokenCreationTest() throws Exception {

        RequestJoin form = new RequestJoin();
        form.setEmail("user01@test.org");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setMobile("01010001000");
        form.setName("사용자01");
        form.setTermsAgree(true);
        joinService.process(form);

        RequestToken form2 = new RequestToken();
        form2.setEmail("user01@test.org");
        form2.setPassword("_aA123456");
        String body = om.writeValueAsString(form2);

        String token = mockMvc.perform(post("/api/v1/member/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString(); // 응답 body 데이터를 반환

        // 회원전용, 관리자 전용 접근 테스트
        mockMvc.perform(get("/api/v1/member/test2")
                .header("Authorization", "Bearer " + token))
                .andDo(print());

    }
}

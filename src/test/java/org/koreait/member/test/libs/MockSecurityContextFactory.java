package org.koreait.member.test.libs;

import org.koreait.member.MemberInfo;
import org.koreait.member.entities.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;

public class MockSecurityContextFactory implements WithSecurityContextFactory<MockMember> {

    @Override
    public SecurityContext createSecurityContext(MockMember annotation) {

        Member member = new Member();
        member.setSeq(annotation.seq());
        member.setEmail(annotation.email());
        member.setPassword(annotation.password());
        member.setName(annotation.name());
        member.setMobile(annotation.mobile());
        member.setAuthority(annotation.authority());
        member.setCredentialChangedAt(LocalDateTime.now().plusMonths(1L));

        MemberInfo memberInfo = MemberInfo.builder()
                .member(member)
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(memberInfo, null, memberInfo.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication); // 로그인 처리

        return context;
    }
}

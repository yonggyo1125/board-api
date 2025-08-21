package org.koreait.member.services;

import lombok.RequiredArgsConstructor;
import org.koreait.member.constants.Authority;
import org.koreait.member.controllers.RequestJoin;
import org.koreait.member.entities.Member;
import org.koreait.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Lazy
@Service
@RequiredArgsConstructor
public class JoinService {
    private final MemberRepository repository;
    private final PasswordEncoder encoder;
    private final ModelMapper mapper;

    public void process(RequestJoin form) {
        Member member = mapper.map(form, Member.class);

        String password  = form.getPassword();
        if (StringUtils.hasText(password)) {
            member.setPassword(encoder.encode(password));
        }

        member.setCredentialChangedAt(LocalDateTime.now());
        member.setAuthority(Authority.MEMBER);

        String mobile = form.getMobile();
        if (StringUtils.hasText(mobile)) {
            mobile = mobile.replaceAll("\\D", "");
            member.setMobile(mobile);
        }

        repository.saveAndFlush(member);
    }
}

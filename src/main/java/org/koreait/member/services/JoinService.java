package org.koreait.member.services;

import lombok.RequiredArgsConstructor;
import org.koreait.file.services.FileUploadService;
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
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
public class JoinService {
    private final FileUploadService uploadService;
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

        String gid = form.getGid();
        gid = StringUtils.hasText(gid) ? gid : UUID.randomUUID().toString();

        member.setGid(gid);

        repository.saveAndFlush(member);

        // 파일 업로드 완료 처리
        uploadService.processDone(gid);
    }
}

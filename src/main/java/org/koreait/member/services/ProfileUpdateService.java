package org.koreait.member.services;

import lombok.RequiredArgsConstructor;
import org.koreait.file.services.FileUploadService;
import org.koreait.member.controllers.RequestProfile;
import org.koreait.member.entities.Member;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final MemberRepository repository;
    private final FileUploadService uploadService;
    private final PasswordEncoder encoder;
    private final MemberUtil memberUtil;

    public Member process(RequestProfile form) {
        Member member = memberUtil.getMember();

        member.setName(form.getName());
        member.setMobile(form.getMobile());

        String password = form.getPassword();
        if (StringUtils.hasText(password)) {
            member.setPassword(encoder.encode(password));
            member.setCredentialChangedAt(LocalDateTime.now());
        }

        String gid = member.getGid();
        if (!StringUtils.hasText(gid)) {
            gid = UUID.randomUUID().toString();
            member.setGid(gid);
        }

        repository.saveAndFlush(member);

        // 파일 업로드 완료 처리
        uploadService.processDone(gid);

        return member;
    }
}

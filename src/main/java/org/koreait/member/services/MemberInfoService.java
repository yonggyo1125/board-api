package org.koreait.member.services;

import lombok.RequiredArgsConstructor;
import org.koreait.file.services.FileInfoService;
import org.koreait.member.MemberInfo;
import org.koreait.member.entities.Member;
import org.koreait.member.repositories.MemberRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    private final MemberRepository repository;
    private final FileInfoService fileInfoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = repository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));

        addInfo(member); // 추가 정보 처리

        return MemberInfo.builder()
                .member(member)
                .build();
    }

    /**
     * 회원정보 추가 처리
     *
     * @param member
     */
    private void addInfo(Member member) {
        String gid = member.getGid();
        if (gid != null) {
            member.setProfileImage(fileInfoService.get(gid));
        }
    }
}

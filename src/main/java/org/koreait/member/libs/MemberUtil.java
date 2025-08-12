package org.koreait.member.libs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.member.MemberInfo;
import org.koreait.member.constants.Authority;
import org.koreait.member.entities.Member;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberUtil {
    private final HttpServletRequest request;

    public boolean isLogin() {
        return getMember() != null;
    }

    public boolean isAdmin() {
        return isLogin() && getMember().getAuthority() == Authority.ADMIN;
    }

    public Member getMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MemberInfo memberInfo) {
            return memberInfo.getMember();
        }

        return null;
    }

    /**
     * 회원 구분 해시
     *  비회원 : 요청헤더 - User-Hash가 있으면 그걸로 대체
     *  회원 : 회원 번호
     * @return
     */
    public int getUserHash() {
        String userHash = request.getHeader("User-Hash");
        userHash = StringUtils.hasText(userHash) ? userHash : UUID.randomUUID().toString();

        return isLogin() ? Objects.hash(getMember().getSeq()) : Objects.hash(userHash);
    }
}

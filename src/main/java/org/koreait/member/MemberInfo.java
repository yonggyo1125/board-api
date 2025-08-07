package org.koreait.member;

import lombok.Builder;
import lombok.Data;
import org.koreait.member.entities.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class MemberInfo implements UserDetails {

    private Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member == null ? null : List.of(new SimpleGrantedAuthority(member.getAuthority().name()));
    }

    @Override
    public String getPassword() {
        return member == null ? null : member.getPassword();
    }

    @Override
    public String getUsername() {
        return member == null ? null : member.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return member != null && member.getDeletedAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return member != null && member.getCredentialChangedAt().isAfter(LocalDateTime.now().minusDays(30L));
    }

    @Override
    public boolean isAccountNonExpired() {
        return member != null && member.getExpired() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return member != null && !member.isLocked();
    }
}

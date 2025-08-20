package org.koreait.member.repositories;

import org.koreait.member.constants.SocialChannel;
import org.koreait.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Member> findBySocialChannelAndSocialToken(SocialChannel channel, String socialToken);
}

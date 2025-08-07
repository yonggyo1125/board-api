package org.koreait.member.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.koreait.member.MemberInfo;
import org.koreait.member.entities.Member;
import org.koreait.member.services.MemberInfoService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Lazy
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class TokenService {
    private final JwtProperties properties;
    private final MemberInfoService infoService;

    private Key key; // private key

    public TokenService(JwtProperties properties, MemberInfoService infoService) {
        this.properties = properties;
        this.infoService = infoService;

        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 발급
     *
     * @param email
     * @return
     */
    public String create(String email) {

        MemberInfo userDetails = (MemberInfo)infoService.loadUserByUsername(email);
        Member member = userDetails.getMember();

        Date date = new Date(new Date().getTime() + properties.getValidTime() * 1000);

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("authority", member.getAuthority())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(date)
                .compact();
    }

    /**
     * JWT 토큰으로 인증 처리(로그인 처리)
     *
     * 요청헤더
     *      Authorization: Bearer 토큰
     * @param token
     * @return
     */
    public Authentication authenticate(String token) {
        return null;
    }
}

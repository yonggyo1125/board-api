package org.koreait.member.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.koreait.global.exceptions.UnAuthorizedException;
import org.koreait.global.libs.Utils;
import org.koreait.member.MemberInfo;
import org.koreait.member.constants.Authority;
import org.koreait.member.constants.SocialChannel;
import org.koreait.member.entities.Member;
import org.koreait.member.exceptions.MemberNotFoundException;
import org.koreait.member.repositories.MemberRepository;
import org.koreait.member.services.MemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Lazy
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class TokenService {
    private final JwtProperties properties;
    private final MemberInfoService infoService;
    private final MemberRepository repository;

    @Autowired
    private Utils utils;

    private Key key; // private key

    public TokenService(JwtProperties properties, MemberInfoService infoService, MemberRepository repository) {
        this.properties = properties;
        this.infoService = infoService;
        this.repository = repository;

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
     * 소셜 로그인시 소셜 채널과 발급 받은 아이디(토큰)으로 회원이 있는지 체크
     * @param channel
     * @param token
     * @return
     */
    public String create(SocialChannel channel, String token) {
        Member member = repository.findBySocialChannelAndSocialToken(channel, token).orElseThrow(MemberNotFoundException::new);

        return create(member.getEmail());
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

        // 토큰 유효성 검사
        validate(token);

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getPayload();

        String email = claims.getSubject();
        Authority authority = Authority.valueOf((String)claims.get("authority"));

        MemberInfo userDetails = (MemberInfo) infoService.loadUserByUsername(email);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority.name()));
        userDetails.getMember().setAuthority(authority);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 처리(로그인 처리)

        return authentication;
    }

    /**
     * 요청헤더
     *  Authorization: Bearer JWT 토큰 ....
     * @param request
     * @return
     */
    public Authentication authenticate(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            return null;
        }

        token = token.substring(7);
        if (!StringUtils.hasText(token)) {
            return null;
        }

        return authenticate(token);
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token
     */
    public void validate(String token) {
        String errorCode = null;
        Exception error = null;

        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getPayload();
        } catch (ExpiredJwtException e) { // 토큰 만료
            errorCode = "JWT.expired";
            error = e;
        } catch (MalformedJwtException | SecurityException e) { // JWT 형식 오류
            errorCode = "JWT.malformed";
            error = e;
        } catch (UnsupportedJwtException e) {
            errorCode = "JWT.unsupported";
            error = e;
        } catch (Exception e) {
            errorCode = "JWT.error";
            error = e;
        }

        if (StringUtils.hasText(errorCode)) {
            throw new UnAuthorizedException(utils.getMessage(errorCode));
        }

        if (error != null) {
            error.printStackTrace();
        }
    }
}

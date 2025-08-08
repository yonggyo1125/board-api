package org.koreait.member.test.libs;

import org.koreait.member.constants.Authority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockMember {
    long seq() default 1L;
    String email() default "testUser@test.org";
    String password() default "_aA123456";
    String name() default "테스트 사용자";
    String mobile() default "01010001000";
    Authority authority() default Authority.MEMBER;
}

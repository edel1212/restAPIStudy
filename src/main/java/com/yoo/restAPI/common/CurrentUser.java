package com.yoo.restAPI.common;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)      // 파라미터 형태로 사용 명시
@Retention(RetentionPolicy.RUNTIME) // 언제까지 해당 어노테이션 지정 여부 : 런타임
// @AuthenticationPrincipal(expression = "targetKey")
/**
 * ✅ 접근 대상이 anonymousUser 권한이 들어올 경우 User 객체를 타고 넘어오지 않아 응답 값이
 *    anonymousUser라는 문자열로 들어오기에 해당 expression의 유연함을 활용해서 적용해주자
 * */
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : targetKey")
public @interface CurrentUser {
}

package com.yoo.restAPI.common;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)      // 파라미터 형태로 사용 명시
@Retention(RetentionPolicy.RUNTIME) // 언제까지 해당 어노테이션 지정 여부 : 런타임
@AuthenticationPrincipal(expression = "targetKey")
public @interface CurrentUser {
}

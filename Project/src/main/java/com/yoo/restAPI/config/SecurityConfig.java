package com.yoo.restAPI.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Log4j2
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        log.info("-------------------------");
        log.info("Filter Chain");
        log.info("-------------------------");

        // 👉  Default Login form 설정
        http.formLogin(Customizer.withDefaults());

        // 👉 권한 별 접근 허용 설정
        http.authorizeHttpRequests( authorize ->
                // ✨ GET 요청은 누구나 접근 가능
                authorize.requestMatchers(HttpMethod.GET, "/api/**")
                        .anonymous()   // 누구나 접근 가능
                        //.authenticated() // 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                );

        return http.build();
    }




    // Security에서 검사 대상 제외 추가
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        log.info("-------------------------");
        log.info("ignoring!!");
        log.info("-------------------------");
        return (web) -> web.ignoring().requestMatchers("/docs/index.html")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }


}

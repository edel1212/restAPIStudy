package com.yoo.restAPI.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

        http.authorizeHttpRequests( authorize ->
                authorize.requestMatchers("/**").hasRole("ADMIN")
                        .anyRequest().authenticated() // 모든 요청은 인증되어야 함
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

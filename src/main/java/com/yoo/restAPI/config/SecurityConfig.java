package com.yoo.restAPI.config;

import com.yoo.restAPI.accounts.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 👉 설정 순간부터 Security 설정은 작성 기준으로 변경된다
public class SecurityConfig  extends WebSecurityConfiguration {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

}

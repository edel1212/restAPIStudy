package com.yoo.restAPI.config;

import com.yoo.restAPI.accounts.Account;
import com.yoo.restAPI.accounts.AccountRole;
import com.yoo.restAPI.accounts.AccountService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@Log4j2
public class AppConfig {
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    // 페스워드 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder(){
        // ✨ 패스워드 앞에 prefix를 추가하여 생성해준다
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    public ApplicationRunner applicationRunner(){
        return new ApplicationRunner() {
            @Autowired
            AccountService accountService;
            @Override
            public void run(ApplicationArguments args) throws Exception {
                log.info("--------------------");
                log.info("서버 실행 시 테스트 아이디 생성");
                log.info("--------------------");
                Account edel1212 = Account.builder()
                        .email("edel1212@naver.com")
                        .password("123")
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
                accountService.registerAccount(edel1212);
            }
        };
    }

}



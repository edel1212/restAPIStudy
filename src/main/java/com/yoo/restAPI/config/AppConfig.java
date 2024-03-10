package com.yoo.restAPI.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
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
}



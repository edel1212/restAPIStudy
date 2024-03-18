package com.yoo.restAPI.config;

import com.yoo.restAPI.accounts.Account;
import com.yoo.restAPI.accounts.AccountRole;
import com.yoo.restAPI.accounts.AccountService;
import com.yoo.restAPI.common.BaseControllerTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTests {

    @Autowired
    AccountService accountService;

    @Test
    @DisplayName("인증 토큰을 받는 테스트")
    void getAuthToken() throws Exception{
        // Given
        String username = "edel1212@naver.com";
        String password = "123";
        Account yoo = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.registerAccount(yoo);

        String clientId = "myApp";
        String clientSecret = "pass";
        this.mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(clientId,clientSecret))
                        .param("username",username)
                        .param("password",password)
                        .param("grant_type","password")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("access_token").exists())
        ;
    }
}
package com.yoo.restAPI.accounts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByUserName() {
        //Given
        String password = "123";
        String username = "edel1212@naver.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.USER,AccountRole.ADMIN))
                .build();
        // 👉 저장
        accountRepository.save(account);

        //When
        // 1 . UserDetailsService를 구현한 구현체 클래스를 가져옴
        UserDetailsService userDetailsService = accountService;
        // 2 . loadUserByUsername()를 통해 UserDetails를 가져옴
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);


        //Then
        System.out.println("-----------");
        System.out.println(password);
        System.out.println(userDetails.getPassword());
        System.out.println("-----------");
        assertThat(userDetails.getPassword()).isEqualTo(password);

    }

    @Test
    @DisplayName("유저를 찾을 수 없는 에러")
    void findByUserNameFail() {
        String username = "unknown";
        try {
            accountService.loadUserByUsername(username);
            fail("supposed to be failed");
        }  catch (UsernameNotFoundException e){
            // 에러 메세지에 유저네임이 있는지 확인
            assertThat(e.getMessage()).contains(username);
        }// try - catch

    }
}
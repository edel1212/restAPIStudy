package com.yoo.restAPI.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
        String username = "keesum@email.com";
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

        assertThat(userDetails.getPassword()).isEqualTo(password);

    }
}
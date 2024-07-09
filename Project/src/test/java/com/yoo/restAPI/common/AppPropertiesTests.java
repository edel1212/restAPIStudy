package com.yoo.restAPI.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppPropertiesTests {

    @Autowired
    private AppProperties properties;

    @Test
    void name() {
        System.out.println("userName  :: " + properties.getUsername());
        System.out.println("userPassword  :: " + properties.getPassword());
    }
}

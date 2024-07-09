package com.yoo.restAPI.common;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@TestConfiguration // ✨ 테스트용 설정
public class RestDocsConfiguration {
    @Bean // ✨ Bean Factory 스캔 대상 추가
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcBuilderCustomizer(){
        return config -> config.operationPreprocessors()
                // ✏️ prettyPrint() 살정을 통해 요청, 응답 커스텀
                .withResponseDefaults(prettyPrint())
                .withRequestDefaults(prettyPrint());
    }
}

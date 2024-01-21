package com.yoo.restAPI;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestApiApplication.class, args);
	}

	/** Dependencies에 추가한 ModelMapper를 빈으로 추가 */
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

}

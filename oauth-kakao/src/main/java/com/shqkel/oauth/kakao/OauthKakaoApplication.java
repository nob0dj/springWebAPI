package com.shqkel.oauth.kakao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.shqkel.oauth.kakao"})
public class OauthKakaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthKakaoApplication.class, args);
	}

}

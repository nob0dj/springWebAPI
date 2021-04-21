package com.shqkel.oauth.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shqkel.oauth.kakao.rest.controller.KakaoRestController;

@SpringBootTest
class OauthKakaoApplicationTests {

	@Autowired
	KakaoRestController controller;
	
	@Test
	void contextLoads() {
		assertNotNull(controller);
	}

}

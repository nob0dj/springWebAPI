package com.login.sns.facebook.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/facebook")
public class FacebookLoginController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping("/login")
    public void login() {
		
    }
	
	@RequestMapping("/callback")
	@ResponseBody
    public void callback(@RequestParam String id, @RequestParam String name) {
		
		logger.info("userId={}, userName={}", id, name);
    }
}

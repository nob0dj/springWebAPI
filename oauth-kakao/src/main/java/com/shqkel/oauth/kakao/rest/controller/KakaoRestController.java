package com.shqkel.oauth.kakao.rest.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.shqkel.oauth.kakao.rest.model.service.KakaoRestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/rest")
@SessionAttributes(value = {"loginUser", "access_token"})
public class KakaoRestController {

	
	private KakaoRestService kakaoRestService;
	
	
	@Autowired
	public void setKakaoRestService(KakaoRestService kakaoRestService) {
		this.kakaoRestService = kakaoRestService;
	}

	/**
	 * value값이 "/"일때는 /rest(x) /rest/(o)
	 * value값이 ""일때는 /rest(o) /rest/(o)
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("")
	public String rest(Model model) {
		return "rest/index";
	}
	
	@GetMapping("/redirect")
	public String redirect(@RequestParam String code, Model model) {
		log.debug("code = {}", code);
		
		//access_token 요청
		String access_token = kakaoRestService.getAccessToken(code);
		
		//사용자 정보 요청
		Map<String, Object> userInfo = kakaoRestService.getUserInfo(access_token);
	    log.debug("userInfo = {}", userInfo);
	    
	    //세션 loginMember로 등록
	    if (userInfo != null) {
	        model.addAttribute("loginUser", userInfo);
	        model.addAttribute("access_token", access_token);
	    }
		
		return "redirect:/rest";
	}
	
	/**
	 * session에 저장된 access_token속성을 가져오기위해 value속성은 필수로 작성한다.
	 * 
	 * @param sessionStatus
	 * @param access_token
	 * @return
	 */
	@GetMapping("/logout")
	public String logout(SessionStatus sessionStatus, @ModelAttribute(value = "access_token") String access_token) {
		log.debug("access_token = {}", access_token);
		
		//logout처리
		kakaoRestService.logout(access_token);
		
		//session객체 complete marking!
		if(!sessionStatus.isComplete())
			sessionStatus.setComplete();
		return "redirect:/rest";
	}
}

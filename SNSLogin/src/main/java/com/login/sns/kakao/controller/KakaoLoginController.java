package com.login.sns.kakao.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/kakao")
public class KakaoLoginController {

	@RequestMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("kakao/login");
    }

	@RequestMapping("/callback")
	@ResponseBody
	public int callback(HttpServletRequest req, HttpSession session) {
		String id = req.getParameter("id");
		String nickname = req.getParameter("nickname");
		String profile_image = req.getParameter("profile_image");
		String thumbnail_image = req.getParameter("thumbnail_image");
		
		System.out.println("id="+id);
		System.out.println("nickname="+nickname);
		System.out.println("profile_image="+profile_image);
		System.out.println("thumbnail_image="+thumbnail_image);
		
		session.setAttribute("user.nickname", nickname);
		session.setAttribute("user.image", thumbnail_image);
		
		return 1;
	}
	
	@RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
    	
    	//@SessionAttribute를 사용하지 않았다면, sessionStatus.setComplete()을 사용할 수 없다.
    	if(session != null)
    		session.invalidate();

    	return new ModelAndView("redirect:/kakao/login");
    }
}

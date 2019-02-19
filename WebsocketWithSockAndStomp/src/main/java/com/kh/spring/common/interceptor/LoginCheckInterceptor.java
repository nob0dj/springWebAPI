package com.kh.spring.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.kh.spring.member.model.vo.Member;

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
	//org.slf4j.Logger타입의 logger를 생성함
	//LoggerFactory.getLogger메소드의 파라미터로 현재클래스객체를 전달함.
	private Logger logger = LoggerFactory.getLogger(LoginCheckInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession();
		Member memberLoggedIn = (Member)session.getAttribute("memberLoggedIn");
		if(memberLoggedIn==null){
			logger.info("비로그인 상태에서 ["+request.getRequestURI()+"] 접근!");
			
			//Referer페이지로 돌려보냄
			String Referer = request.getHeader("Referer");// http://localhost:9090/spring/board/boardView?no=35
			String Origin = request.getHeader("Origin");//chrome[POST] => http://localhost:9999, chrome[GET],ie[GET/POST],firefox[POST] => null
			String url = request.getRequestURL().toString();// http://localhost:9090/spring/member/login.do
			//getRequestURL => StringBuffer => String
			String uri = request.getRequestURI();	// /spring/member/login.do
			
			//크롬외 브라우져용
			if(Origin==null) {
				Origin = url.replace(uri, "");
			}
			
			String loc = Referer.replace(Origin+request.getContextPath(), "");
			
			request.setAttribute("loc", loc);
			request.setAttribute("msg", "로그인후 이용하실 수 있습니다.");
			request.getRequestDispatcher("/WEB-INF/views/common/msg.jsp").forward(request, response);
			return false;
		}
		
		return super.preHandle(request, response, handler);
	}
	
}

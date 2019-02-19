package com.kh.spring.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
* interceptor클라스는 
* HandlerInterceptorAdaptor가  HandlerInterceptor인터페이스를 구현하면서
* Override한 3개의 메소드를 선택적으로 구현하면됨.
*  
* 1. preHandle
* 2. postHandle
* 3. afterCompletion
* 
* @author nobodj
*
*/
public class LoggerInterceptor extends HandlerInterceptorAdapter {
	//org.slf4j.Logger타입의 logger를 생성함
	//LoggerFactory.getLogger메소드의 파라미터로 현재클래스객체를 전달함.
	private Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
		//debug레벨로 로그를 남기는 지 여부 확인. 이는 LoggingEvent로 발생되는 시간을 절약할 수 있음. 
		//log4j.xml에서 해당패키지를 Logger로 등록해야함. level=debug로 해야 아래조건문 출력가능.
		if(logger.isDebugEnabled()){
			logger.debug("=========================== START ==============================");
			logger.debug(request.getRequestURI());
			logger.debug("----------------------------------------------------------------");
		}
		
		return super.preHandle(request, response, handler);	//항상 true를 리턴
	}
	

	@Override
	public void postHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {

		super.postHandle(request, response, handler, modelAndView);

		if(logger.isDebugEnabled()){
			logger.debug("------------------------- view --------------------------------");
		}
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("=========================== END ==============================\n");
		}
		super.afterCompletion(request, response, handler, ex);
	}
}

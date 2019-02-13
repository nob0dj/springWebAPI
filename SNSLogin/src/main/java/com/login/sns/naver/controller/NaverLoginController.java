package com.login.sns.naver.controller;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.login.sns.naver.model.bo.NaverLoginBO;

@Controller
@RequestMapping("/naver")
public class NaverLoginController {
	/* NaverLoginBO */
	private NaverLoginBO naverLoginBO;

	/* NaverLoginBO */
	@Autowired
	private void setNaverLoginBO(NaverLoginBO naverLoginBO){
		this.naverLoginBO = naverLoginBO;
	}
	
	@RequestMapping("/login")
    public ModelAndView login(HttpSession session) {
		 /* 네아로 인증 URL을 생성하기 위하여 getAuthorizationUrl을 호출 */
        String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
        System.out.printf("url=%s\n",naverAuthUrl);
        /* 생성한 인증 URL을 View로 전달 */
        return new ModelAndView("naver/login", "url", naverAuthUrl);
    }
 
    @RequestMapping("/callback")
    public ModelAndView callback(@RequestParam String code, @RequestParam String state, HttpSession session) throws IOException {
    	/* 네아로 인증이 성공적으로 완료되면 code 파라미터가 전달되며 이를 통해 access token을 발급 */
		OAuth2AccessToken oauthToken = naverLoginBO.getAccessToken(session, code, state);
		
		System.out.println("발급받은 accessToken : " + oauthToken);
//		발급받은 accessToken : OAuth2AccessToken{
//			access_token=AAAAOg8rwmFRhFGz2d7vzGaCRrkSMLZjCSfe7jPw9ERTzmgfe1mNFlJ/ypD+DNTe2YmdoiR1wCIDL/1UupTfAjWez5A=, 
//			token_type=bearer, 
//			expires_in=3600, 
//			refresh_token=dZyoqiiiicj0LCkip0Bfd7jVqSipfJavoKLra9cfJzqN32v8HGU93fAA5MisaLMVtUh4vwJKissEh6HDrGiir1fyPbxevhFfZIPXlVnS7wjjipRuCtqg7Robd33fpGjT5039hERw, 
//			scope=null}
		
		//session 속성에 저장된 값 확인
		System.out.println("-------------------------------------------");
		Enumeration<String> attrNames = session.getAttributeNames();
		while(attrNames.hasMoreElements()){
			String key = attrNames.nextElement();
			System.out.printf("%s = %s \n", key, session.getAttribute(key));
		}
		System.out.println("-------------------------------------------");
		/*사용자프로필 조회*/
		String apiResult = naverLoginBO.getUserProfile(oauthToken);
		
		//1.TypeToken객체 사용하기
		//Open Declaration com.google.gson.reflect.TypeToken.TypeToken<E>
//		Gson gson = new Gson(); 
//		Type type = new TypeToken<Map<String, Object>>(){}.getType();
//		Map<String, Object> map = gson.fromJson(apiResult, type);
		
		//2.com.google.gson.internal.LinkdeTreeMap 객체 사용하기
		Gson gson = new Gson();
		/*pom.xml에서 gson버젼확인 2.6.2에서 LinkedTreeMap클래스 사용가능*/
		LinkedTreeMap map = gson.fromJson(apiResult , LinkedTreeMap.class);
		
		String resultCode = (String)map.get("resultcode");
		String message = (String)map.get("message");
		LinkedTreeMap response = (LinkedTreeMap)map.get("response");
		String id = (String)response.get("id");
		String name = (String)response.get("name");
		String nickname = (String)response.get("nickname");
		String profile_image = (String)response.get("profile_image");
		String age = (String)response.get("age");
		String gender = (String)response.get("gender");
		String email = (String)response.get("email");
		String birthday = (String)response.get("birthday");
		
		
		System.out.printf("resultCode=%s\n", resultCode);
		System.out.printf("message=%s\n", message);
		System.out.printf("id=%s\n", id);
		System.out.printf("name=%s\n", name);
		System.out.printf("nickname=%s\n", nickname);
		System.out.printf("profile_image=%s\n", profile_image);
		System.out.printf("age=%s\n", age);
		System.out.printf("gender=%s\n", gender);
		System.out.printf("email=%s\n", email);
		System.out.printf("birthday=%s\n", birthday);
		
		System.out.printf("resultMap=%s\n", map);

		//사용자이름 저장
		session.setAttribute("name", name);
		
		return new ModelAndView("naver/callback", "result", map);
    }
    
    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
    	
    	//@SessionAttribute를 사용하지 않았다면, sessionStatus.setComplete()을 사용할 수 없다.
    	if(session != null)
    		session.invalidate();

    	return new ModelAndView("redirect:/naver/login");
    }
}

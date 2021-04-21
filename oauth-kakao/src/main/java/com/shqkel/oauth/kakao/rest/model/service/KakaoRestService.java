package com.shqkel.oauth.kakao.rest.model.service;

import java.util.Map;

public interface KakaoRestService {

	public String getAccessToken (String authorize_code);
	
	public Map<String, Object> getUserInfo (String access_token);
	
	public void logout(String access_token);
}

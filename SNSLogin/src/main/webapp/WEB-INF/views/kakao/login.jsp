<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Kakao Login" name="pageTitle"/>
</jsp:include>
<%
	String nickname = (String)session.getAttribute("user.nickname");
	String image = (String)session.getAttribute("user.image");
%>

<div style="text-align:cener;">
<% if(nickname == null){ %>  
  <a id="kakao-login-btn"></a>
  
<% } else { %>  

	<img src="<%=image %>" />&nbsp;
	<strong><%=nickname %></strong>님, 환영합니다. &nbsp;&nbsp;
	<a href="${pageContext.request.contextPath }/kakao/logout">logout하기</a>
	<!-- 카카오톡이 제공하는 로그아웃이미지는 없다. -->
  
<% } %>
</div>


<script src="https://developers.kakao.com/sdk/js/kakao.min.js"></script>
<script type='text/javascript'>
// 사용할 앱의 JavaScript 키를 설정해 주세요.
Kakao.init('a37bc17fde83ce7beabdc5704c667b6c');

<% if(nickname == null){ %>  
// 카카오 로그인 버튼을 생성하고, 클릭했을때 핸들러 함수를 지정.
Kakao.Auth.createLoginButton({
  container: '#kakao-login-btn',
  success: function(authObj) {
 	console.log("authObj",authObj);
 	/*
 	authObj = {
 		access_token: "xI3zLaKyu-gqmqvu_xM5LpD9tK0cPzncTuedSgopyNgAAAFsLLlE8A",
		expires_in: 7199,
		refresh_token: "RkmAWp52FxcLCT0lwkp2iOxOW5RWa9WuhTgt3gopyNgAAAFsLLlE7w",
		refresh_token_expires_in: 5183999,
		scope: "profile",
		stateToken: "0doq7wft0qgi6q9njmowc4j",
		token_type: "bearer"
 	}
 	
 	
 	*/
 	
 	//인증에 성공한 경우 사용자 프로필정보 가져오기
 	getUserProfile(); 	
  },
  fail: function(err) {
  	alert(JSON.stringify(err));
  }
});

function getUserProfile(){
	Kakao.API.request({
		url: '/v1/user/me',
		success: function(user){
			console.log("user",user);
			
			/*
			user = {
				id: 1001379012,
				properties: {
					nickname: "김동현",
					profile_image: "http://k.kakaocdn.net/dn/lC7Wl/btqrI9b9HoJ/rZmUnEFEnR2HiYJfROc7pk/profile_640x640s.jpg",
					thumbnail_image: "http://k.kakaocdn.net/dn/lC7Wl/btqrI9b9HoJ/rZmUnEFEnR2HiYJfROc7pk/profile_110x110c.jpg"
				}
			}
			*/
			
			//userLogin(user);
		},
		fail: function(err) {
			alert(JSON.stringify(err));
	  	}
	})
}

/** 
 * 사용자 정보를 서버측에 전송해서 HttpSession객체를 갱신한다.
 * success함수에서 현재페이지를 새로고침해서, 로그인한 사용자용 페이지를 전송함.
 */
function userLogin(user){
	var param = {
			id: user.id,
			nickname: user.properties.nickname,
			profile_image: user.properties.profile_image,
			thumbnail_image: user.properties.thumbnail_image
	}
	console.log(param);
	
	$.ajax({
		url: "${pageContext.request.contextPath}/kakao/callback",
		data: param,
		type: "post",
		success: function(data){
			//console.log(data);
			
			//페이지 새로고침: 카카오api통신 내용을 확인하고 싶으면 주석처리할것.
			//location.reload();
		}
	});
}

<% } %>  


function userLogout(){
	Kakao.Auth.logout(function(){
		location.reload();
	});
}
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>

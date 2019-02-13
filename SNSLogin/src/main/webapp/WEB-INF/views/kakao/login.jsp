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
// 카카오 로그인 버튼을 생성합니다.
Kakao.Auth.createLoginButton({
  container: '#kakao-login-btn',
  success: function(authObj) {
 	console.log(authObj);
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
			//console.log(user);
			//console.log(user.properties);
			
			userLogin(user);
		},
		fail: function(err) {
			alert(JSON.stringify(err));
	  	}
	})
}

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
			location.reload();
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

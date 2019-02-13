<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Naver Login" name="pageTitle"/>
</jsp:include>

<div style="text-align:center;">
<%-- ${result} --%>
	<table border=1 width=300px align=center>
		<tr>
			<th>resultCode</th>
			<td>${result.resultcode}</td>
		</tr>
		<tr>
			<th>message</th>
			<td>${result.message}</td>
		</tr>
		<tr>
			<th>id</th>
			<td>${result.response.id}</td>
		</tr>
		<tr>
			<th>name</th>
			<td>${result.response.name}</td>
		</tr>
		<tr>
			<th>nickname</th>
			<td>${result.response.nickname}</td>
		</tr>
		<tr>
			<th>profile_image</th>
			<td>
				<img src="${result.response.profile_image}" width="100px" />
			</td>
		</tr>
		<tr>
			<th>age</th>
			<td>${result.response.age}</td>
		</tr>
		<tr>
			<th>gender</th>
			<td>${result.response.gender}</td>
		</tr>
		<tr>
			<th>email</th>
			<td>${result.response.email}</td>
		</tr>
		<tr>
			<th>birthday</th>
			<td>${result.response.birthday}</td>
		</tr>
		
	</table>
	
	<br />
	<br />
	<a href="${pageContext.request.contextPath}/naver/logout">
		<img src="${pageContext.request.contextPath}/resources/images/naver/네이버 아이디로 로그인_로그아웃_Green.PNG" width=100px />
	</a>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>

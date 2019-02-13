<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Naver Login" name="pageTitle"/>
</jsp:include>
<%
	String name = (String)session.getAttribute("name");
%>

<div style="text-align:center">
<% if(name == null){ %>
	<a href="${url}">
		<img src="${pageContext.request.contextPath}/resources/images/naver/네이버 아이디로 로그인_완성형_Green.PNG" width=200px />
	</a>
<% } 
else {%>
	<strong><%=session.getAttribute("name") %></strong>님, 환영합니다.
	&nbsp;&nbsp;
	<a href="${pageContext.request.contextPath}/naver/logout">
		<img src="${pageContext.request.contextPath}/resources/images/naver/네이버 아이디로 로그인_로그아웃_Green.PNG" width=100px />
	</a>
<% } %>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>

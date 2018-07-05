<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="GS25 이벤트" name="pageTitle"/>
</jsp:include>
<table class="table">
	<tr>
		<th>이벤트명</th>
		<th>이벤트기간</th>		
		<th>이미지</th>
	</tr>
	<c:forEach items="${data}" var="e">
	<tr onclick="location.href='${e['a.href']}'" style="cursor:pointer;">
		<td>${e["eventTitle"]}</td>
		<td>${e["period"] }</td>
		<td><img src="${e['img.src']}" width="100px"></td>
	</tr>
	</c:forEach>
</table>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
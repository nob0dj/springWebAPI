<%@page import="java.io.PrintStream"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Error</title>
<style>
div#error-container{text-align:center;}
</style>
</head>
<body>
	<div id="error-container">
		<h1>Error</h1>
		<!-- exception�?체는 el로 접근할 수 없�?�. -->
		<%-- <h2>${exception.message}</h2> --%>
		<h2 style="color:red;"><%= exception.getMessage() %></h2>
		<a href="${pageContext.request.contextPath }">첫페�?�지로 �?�아가기</a>
		<!--
		예외발�? Stack Trace : 
		<% 
		StringBuffer sb = new StringBuffer(500);
	    StackTraceElement[] st = exception.getStackTrace();
	    sb.append(exception.getClass().getName() + ": " + exception.getMessage() + "\n");
	    for (int i = 0; i < st.length; i++) {
	      sb.append("\t at " + st[i].toString() + "\n");
	    }
		%>
		<%=sb%>
		 -->
	</div>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${param.pageTitle}</title>
<script src="${pageContext.request.contextPath }/resources/js/jquery-3.2.1.min.js"></script>
<!-- �??��?��?��?���??�� ?��?��브러�? -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css" integrity="sha384-9gVQ4dYFwwWSjIDZnLEWnxCjeSWFphJiwGPXr1jddIhOegiu1FwO5qRGvFXOdJZ4" crossorigin="anonymous">
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js" integrity="sha384-uefMccjFJAIv6A+rW+L4AHf99KvxDjWSu1z9VI8SKNVmz4sk7buKt/6v9KI65qnm" crossorigin="anonymous"></script>
<!-- ?��?��?��?��?�� css -->
<link rel="stylesheet" href="${pageContext.request.contextPath }/resources/css/style.css" />


<!-- WebSocket:sock.js CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.3.0/sockjs.js"></script>
<!-- WebSocket: stomp.js CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.js"></script>
</head>
<body>
<div id="container">
	<header>
		<div id="header-container">
			<h2>${param.pageTitle}</h2>
		</div>
		<nav class="navbar navbar-expand-lg navbar-light bg-light">
			<a class="navbar-brand" href="#">
				<img src="${pageContext.request.contextPath }/resources/images/logo-spring.png" alt="" width="50px" />
			</a>
		  	<!-- 반응?��?���? width 줄어?��경우, collapse버튼�??�� -->
			<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
		  	</button>
			<div class="collapse navbar-collapse" id="navbarNav">
				<ul class="navbar-nav mr-auto">
			      <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}">Home</a></li>
			      <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/ws/websocket.do">Websocket</a></li>
			      <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/ws/sock.do">sock.js</a></li>
			      <c:if test="${memberLoggedIn==null || \"admin\" ne memberLoggedIn.memberId}">
			      <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/ws/stomp.do">stomp.js</a></li>
				  </c:if>
				  <c:if test="${\"admin\" eq memberLoggedIn.memberId}">
			      <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/ws/admin.do">관리자용 stomp.js</a></li>
				  </c:if>
			    </ul>
			    
				<!-- 로그인 분기 처리  -->
				<c:if test="${memberLoggedIn==null}">
			        <!-- 로그인,회원가입 버튼 -->
			        <button class="btn btn-outline-success my-2 my-sm-0" type="button" data-toggle="modal" data-target="#loginModal">로그인</button>
			        &nbsp;
			        <button class="btn btn-outline-success my-2 my-sm-0" type="button" onclick="location.href='${pageContext.request.contextPath}/member/memberEnroll.do'">회원가입</button>
			    </c:if>
			    <c:if test="${memberLoggedIn!=null}">
			        <%-- <span><a href="#">${memberLoggedIn.memberName}</a> 님, 안녕하세요</span> --%>
			        <span><a href="${pageContext.request.contextPath}/member/memberView.do?memberId=${memberLoggedIn.memberId}" title="내정보보기">${memberLoggedIn.memberName}</a> 님, 안녕하세요</span>
			        &nbsp;
			        <button class="btn btn-outline-success my-2 my-sm-0" type="button" onclick="location.href='${pageContext.request.contextPath}/member/memberLogout.do'">로그아웃</button>
			    </c:if>    
			</div>
		</nav>
		<!-- Modal시작 -->
		<!-- https://getbootstrap.com/docs/4.1/components/modal/#live-demo -->
		<div class="modal fade" id="loginModal" tabindex="-1" role="dialog" aria-labelledby="loginModalLabel" aria-hidden="true">
		  <div class="modal-dialog" role="document">
		    <div class="modal-content">
		      <div class="modal-header">
		        <h5 class="modal-title" id="loginModalLabel">로그인</h5>
		        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
		          <span aria-hidden="true">&times;</span>
		        </button>
		      </div>
	          <!--로그인폼 -->
	          <!-- https://getbootstrap.com/docs/4.1/components/forms/#overview -->
	          <form action="${pageContext.request.contextPath}/member/memberLogin.do" method="post">
		      <div class="modal-body">
				  <input type="text" class="form-control" name="memberId" placeholder="아이디" required>
				    <br />
				    <input type="password" class="form-control" name="password" placeholder="비밀번호" required>
		      </div>
		      <div class="modal-footer">
		        <button type="submit" class="btn btn-outline-success">로그인</button>
		        <button type="button" class="btn btn-outline-success" data-dismiss="modal">취소</button>
		      </div>
			</form>
		    </div>
		  </div>
		</div>
		<!-- Modal 끝-->
	</header>
	<section id="content">

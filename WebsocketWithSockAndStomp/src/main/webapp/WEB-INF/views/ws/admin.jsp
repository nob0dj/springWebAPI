<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="채팅방 관리페이지" name="pageTitle"/>
</jsp:include>
<style>
table.table th, table.table td {text-align: center;}
</style>
<table class="table">
  <thead>
    <tr>
      <th scope="col">#</th>
      <th scope="col">회원아이디</th>
      <th scope="col">메세지</th>
      <th scope="col">안읽은 메세지수</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach items="${recentList }" var="m" varStatus="vs">
    <tr chatNo='<c:out value="${m.CHATID}.${m.MEMBERID}"/>' /><%-- el의 문자열 더하기 연산대신 jstl out태그 사용 --%>
      <th scope="row">${vs.count}</th>
      <td><a href="javascript:goChat('${m.CHATID}')">${m.MEMBERID }</a></td>
      <td>${m.MSG }</td>
      <td><span class="badge badge-light">${m.CNT }</span></td>
    </tr>
  </c:forEach>
  </tbody>
</table>
<script>
//웹소켓 선언
//1.최초 웹소켓 생성 url: /stomp
let socket = new SockJS('<c:url value="/stomp" />');
let stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
	console.log('connected stomp over sockjs');
	console.log(frame);

	// subscribe message
	stompClient.subscribe('/chat/admin', function(message) {
		console.log("receive from /chat/admin :", message);
		//새로운 메세지가 있을때 목록 갱신을 위해서 reload함.
		location.reload();
		//let messsageBody = JSON.parse(message.body);
		//$("#data").append(messsageBody.memberId+":"+messsageBody.msg+ "<br/>");
	});

});

function goChat(chatId){
	open("${pageContext.request.contextPath}/ws/"+chatId+"/adminChat.do/", chatId, "width=500, height=500", false);
}
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>
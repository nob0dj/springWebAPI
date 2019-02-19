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

<div class="input-group mb-3">
  <input type="text" id="message" class="form-control" placeholder="Message">
  <div class="input-group-append" style="padding: 0px;">
    <button id="sendBtn" class="btn btn-outline-secondary" type="button">Send</button>
  </div>
</div>
<div>
	<ul class="list-group list-group-flush" id="data">
	<c:forEach items="${chatList}" var="m">
		<li class="list-group-item">${m.memberId }: ${m.msg }</li>
	</c:forEach>	
	</ul>
</div>
<script type="text/javascript">
$(document).ready(function() {
	$("#sendBtn").click(function() {
		sendMessage();
		$('#message').val('')
	});
	$("#message").keydown(function(key) {
		if (key.keyCode == 13) {// 엔터
			sendMessage();
			$('#message').val('')
		}
	});

	//window focus이벤트핸들러 등록
	$(window).on("focus", function() {
		console.log("focus");
		lastCheck();
	});
});
//윈도우가 활성화 되었을때, chatroom테이블의 lastcheck(number)컬럼을 갱신한다.
//안읽은 메세지 읽음 처리
function lastCheck() {
	let data = {
		chatId : "${chatId}",
		memberId : "${memberLoggedIn.memberId}",
		time : new Date().getTime()
	}
	stompClient.send('<c:url value="/lastCheck" />', {}, JSON.stringify(data));
}

//웹소켓 선언
//1.최초 웹소켓 생성 url: /stomp
let socket = new SockJS('<c:url value="/stomp" />');
let stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
	console.log('connected stomp over sockjs');
	console.log(frame);
	
	//사용자 확인
	lastCheck();
	
	// subscribe message
	stompClient.subscribe('/chat/${chatId}', function(message) {
		console.log("receive from /chat/${chatId} :", message);
		let messsageBody = JSON.parse(message.body);
		$("#data").append("<li class=\"list-group-item\">"+messsageBody.memberId+" : "+messsageBody.msg+ "</li>");
	});

});

function sendMessage() {

	let data = {
		chatId : "${chatId}",
		memberId : "${memberLoggedIn.memberId}",
		msg : $("#message").val(),
		time : new Date().getTime(),
		type: "MESSAGE"
	}

	stompClient.send('<c:url value="/chat/${chatId}" />', {}, JSON.stringify(data));
}


</script>
</body>
</html>
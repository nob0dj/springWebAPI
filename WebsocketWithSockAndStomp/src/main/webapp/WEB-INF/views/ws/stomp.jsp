<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Hello Stomp.js" name="pageTitle"/>
</jsp:include>
<div class="input-group mb-3">
  <input type="text" id="message" class="form-control" placeholder="Message">
  <div class="input-group-append" style="padding: 0px;">
    <button id="sendBtn" class="btn btn-outline-secondary" type="button">Send</button>
  </div>
</div>
<div>
	<ul class="list-group list-group-flush" id="data"></ul>
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
		memberId : "${memberId}",
		time : new Date().getTime()
	}
	stompClient.send('<c:url value="/lastCheck" />', {}, JSON.stringify(data));
}

//웹소켓 선언
//1.최초 웹소켓 생성 url: /stomp
let socket = new SockJS('<c:url value="/stomp" />');
let stompClient = Stomp.over(socket);

//connection이 맺어지면, 콜백함수가 호출된다.
stompClient.connect({}, function(frame) {
	console.log('connected stomp over sockjs');
	console.log(frame);
	
	//사용자 확인
	lastCheck();
	
	//stomp에서는 구독개념으로 세션을 관리한다. 핸들러 메소드의 @SendTo어노테이션과 상응한다.
	stompClient.subscribe('/hello', function(message) {
		console.log("receive from /hello :", message);
		let messsageBody = JSON.parse(message.body);
		$("#data").append("<li class=\"list-group-item\">"+messsageBody.memberId+" : "+messsageBody.msg+ "</li>");
	});

	//stomp에서는 구독개념으로 세션을 관리한다. 핸들러 메소드의 @SendTo어노테이션과 상응한다.
	stompClient.subscribe('/chat/${chatId}', function(message) {
		console.log("receive from /subscribe/stomp/abcde :", message);
		let messsageBody = JSON.parse(message.body);
		$("#data").append("<li class=\"list-group-item\">"+messsageBody.memberId+" : "+messsageBody.msg+ "</li>");
	});

});

function sendMessage() {

	let data = {
		chatId : "${chatId}",
		memberId : "${memberId}",
		msg : $("#message").val(),
		time : new Date().getTime(),
		type: "MESSAGE"
	}

	//테스트용 /hello
	//stompClient.send('<c:url value="/hello" />', {}, JSON.stringify(data));
	
	//채팅메세지: 1:1채팅을 위해 고유한 chatId를 서버측에서 발급해 관리한다.
	stompClient.send('<c:url value="/chat/${chatId}" />', {}, JSON.stringify(data));
}


</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>

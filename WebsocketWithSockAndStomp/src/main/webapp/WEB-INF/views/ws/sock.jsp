<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Hello Sock.js" name="pageTitle"/>
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
});

// 웹소켓을 지정한 url로 연결한다.
//let sock = new SockJS("<c:url value="/echo"/>");
let sock = new SockJS("http://"+window.location.host+"${pageContext.request.contextPath}/echo");
sock.onopen = onOpen;
sock.onmessage = onMessage;
sock.onclose = onClose;

// 웹소켓 연결성공후
function onOpen(){
	console.log('websocket opened');
}

// 서버로부터 메시지를 받았을 때
function onMessage(msg) {
       var data = msg.data;
       $("#data").append("<li class=\"list-group-item\">"+data+ "</li>");
}

// 서버와 연결을 끊었을 때
function onClose(evt) {
       $("#data").append("연결 끊김");
}


//메시지 전송
function sendMessage() {
    sock.send($("#message").val());
}

</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="Hello Spring" name="pageTitle"/>
</jsp:include>
<table class="table">
	<tr>
		<th>이벤트명</th>
		<th>이벤트기간</th>
		<th>이미지</th>
	</tr>
	<c:forEach items="${sevenElevenEventList}" var="e">
	<tr onclick="${e['a.href']}" style="cursor:pointer;">
		<td>${e["dt"]}</td>
		<td>${e["dd"]}</td>
		<td><img src="http://7-eleven.co.kr/${e['img.src']}" width="100px"></td>
	</tr>
	</c:forEach>
</table>
<form name="actFrm" id="actFrm" action="" method="post" accept-charset="utf-8">
	<input type="hidden" name="seqNo" id="seqNo" value="" title="시퀀스 번호">		
	<input type="hidden" name="intPageSize" id="intPageSize" value="8" title="더보기 클릭시 추가 리스트 수">		
	<input type="hidden" name="listNo" id="listNo" value="" title="다른이벤트보기 순서">		
</form>
<script>
var fncGoView = function(objVal, listNo){
	$("#seqNo").val(objVal);
	$("#listNo").val(listNo);
	$("#intPageSize").val(8);

//	$("#actFrm").attr("target", "ifrmAction");
	$("#actFrm").attr("target", "_self");
//	$("#actFrm").attr("enctype", "multipart/form-data");
	$("#actFrm").attr("action","http://www.7-eleven.co.kr/event/eventView.asp").submit();
};

</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
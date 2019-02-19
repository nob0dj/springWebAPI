<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:include page="/WEB-INF/views/common/header.jsp">
	<jsp:param value="회원등록" name="pageTitle"/>
</jsp:include>

<style>
div#enroll-container{width:400px; margin:0 auto; text-align:center;}
div#enroll-container input, div#enroll-container select {margin-bottom:10px;}
div#enroll-container table th{text-align: right; padding-right:10px;}
div#enroll-container table td{text-align: left;}
/*중복아이디체크관련*/
div#memberId-container{position:relative; padding:0px;}
div#memberId-container span.guide {display:none;font-size: 12px;position:absolute; top:12px; right:10px;}
div#memberId-container span.ok{color:green;}
div#memberId-container span.error{color:red;}
</style>
<div id="enroll-container">
	<form name="memberEnrollFrm" action="memberEnrollEnd.do" method="post" onsubmit="return validate();" >
		<table>
			<tr>
				<th>아이디</th>
				<td>
					<div id="memberId-container">
						<input type="text" class="form-control" placeholder="4글자이상" name="memberId" id="memberId_" required>
						<!-- 아이디중복검사 코멘트추가 -->
						<span class="guide ok">이 아이디는 사용가능합니다.</span>
	            		<span class="guide error">이 아이디는 사용할 수 없습니다.</span>
	            		<input type="hidden" name="idDuplicateCheck" id="idDuplicateCheck" value="0"/>
	            	</div>
				</td>
			</tr>
			<tr>
				<th>패스워드</th>
				<td>
					<input type="password" class="form-control" name="password" id="password_" required>
				</td>
			</tr>
			<tr>
				<th>패스워드확인</th>
				<td>	
					<input type="password" class="form-control" id="password2" required>
				</td>
			</tr>  
			<tr>
				<th>이름</th>
				<td>	
				<input type="text" class="form-control" name="memberName" id="memberName" required>
				</td>
			</tr>
			<tr>
				<th>나이</th>
				<td>	
				<input type="number" class="form-control" name="age" id="age">
				</td>
			</tr> 
			<tr>
				<th>이메일</th>
				<td>	
					<input type="email" class="form-control" placeholder="abc@xyz.com" name="email" id="email">
				</td>
			</tr>
			<tr>
				<th>휴대폰</th>
				<td>	
					<input type="tel" class="form-control" placeholder="(-없이)01012345678" name="phone" id="phone" maxlength="11" required>
				</td>
			</tr>
			<tr>
				<th>주소</th>
				<td>	
					<input type="text" class="form-control" placeholder="" name="address" id="address">
				</td>
			</tr>
			<tr>
				<th>성별 </th>
				<td>
					<div class="form-check form-check-inline">
						<input type="radio" class="form-check-input" name="gender" id="gender0" value="M" checked>
						<label  class="form-check-label" for="gender0">남</label>&nbsp;
						<input type="radio" class="form-check-input" name="gender" id="gender1" value="F">
						<label  class="form-check-label" for="gender1">여</label>
					</div>
				</td>
			</tr>
			<tr>
				<th>취미 </th>
				<td>
					<div class="form-check form-check-inline">
						<input type="checkbox" class="form-check-input" name="hobby" id="hobby0" value="운동"><label class="form-check-label" for="hobby0">운동</label>&nbsp;
						<input type="checkbox" class="form-check-input" name="hobby" id="hobby1" value="등산"><label class="form-check-label" for="hobby1">등산</label>&nbsp;
						<input type="checkbox" class="form-check-input" name="hobby" id="hobby2" value="독서"><label class="form-check-label" for="hobby2">독서</label>&nbsp;
						<input type="checkbox" class="form-check-input" name="hobby" id="hobby3" value="게임"><label class="form-check-label" for="hobby3">게임</label>&nbsp;
						<input type="checkbox" class="form-check-input" name="hobby" id="hobby4" value="여행"><label class="form-check-label" for="hobby4">여행</label>&nbsp;
					 </div>
				</td>
			</tr>
		</table>
		<input type="submit" value="가입" >
		<input type="reset" value="취소">
	</form>
</div>
<script>
$(function(){
	
	$("#password2").blur(function(){
		var p1=$("#password_").val(), p2=$("#password2").val();
		if(p1!=p2){
			alert("패스워드가 일치하지 않습니다.");
			$("#password_").focus();
		}
	});
	
	/* 아이디 중복검사 이벤트 추가 */
	$("#memberId_").on("keyup", function(){
        var memberId = $(this).val().trim();
        if(memberId.length<4) return;
        //console.log(memberId);
        
        $.ajax({
            url  : "${pageContext.request.contextPath}/member/checkIdDuplicate.do",
            data : {memberId:memberId},
            dataType: "json",
            success : function(data){
                console.log(data);
                //if(data=="true"){//stream 방식
                if(data.isUsable==true){//viewName 방식
                    $(".guide.error").hide();
                    $(".guide.ok").show();
                    $("#idDuplicateCheck").val(1);
                }
                else{
                    $(".guide.error").show();
                    $(".guide.ok").hide();
                    $("#idDuplicateCheck").val(0);
                }
                
            },
            error : function(jqxhr, textStatus, errorThrown){
                console.log("ajax 처리 실패");
                //에러로그
                console.log(jqxhr);
                console.log(textStatus);
                console.log(errorThrown);
            }

        });
        
    });
	
});

function validate(){
	var memberId = $("#memberId_");
	if(memberId.val().trim().length<4){
		alert("아이디는 최소 4자리이상이어야 합니다.");
		memberId.focus();
		return false;
	}
	
	//아이디중복체크여부
    if($("#idDuplicateCheck").val()==0){
        alert("사용가능한 아이디를 입력해주세요.");
        return false();
    }
	
	return true;
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"></jsp:include>
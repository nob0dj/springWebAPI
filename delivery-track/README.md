# 택배조회 api
[택배 배송 조회 API 사용하기(스윗트래커 활용)](https://shlee0882.tistory.com/59) 포스팅을 참조하여 작성함.


## api key 발급
무료버젼 선택후 구매. 한달간 1000건 이용가능. 운송장번호 하나당 1건으로 카운팅하는 듯함.
[https://tracking.sweettracker.co.kr/templates/app.html#/apikey/add](https://tracking.sweettracker.co.kr/templates/app.html#/apikey/add)

## springboot 프로젝트생성
* groupId : com.kh
* artifactId : delivery-track
* srping web, devtools, thymeleaf 의존선택
* war
* springboot버젼: 2.1.3.RELEASE로 변경
* package: com.kh.delivery.track

@src/main/resources/application.yml

    #application.yml
    server:
      port: 9999
      servlet:
        context-path: /delivery-track
      
    #logging
    logging:
      level:
        com.kh.delivery.track: DEBUG
    

@com.kh.delivery.track;
spring explorer view에서 등록된 bean과 request-mapping현황 확인

    @Controller
    public class DeliveryTrackController {
      
      Logger logger = LoggerFactory.getLogger(getClass());
      
      @GetMapping("/")
      public String index(Model model) {
        logger.debug("{}", "[/] : index페이지 요청!");
        model.addAttribute("pageTitle", "스마트택배조회");
        
        Date apiKeyStart = new Date(new GregorianCalendar(2020, 0, 9).getTimeInMillis());
        Date apiKeyEnd = new Date(new GregorianCalendar(2020, 1, 9).getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        
        model.addAttribute("apiKeyStart", dateFormat.format(apiKeyStart));
        model.addAttribute("apiKeyEnd", dateFormat.format(apiKeyEnd));
        model.addAttribute("now", dateFormat.format(new Date()));
        
        return "index";
      }
    }
   

@pom.xml
thymeleaf layout 사용을 위한 의존 추가


    <!-- layout을 사용하기 위한 의존  -->
		<dependency>
		    <groupId>nz.net.ultraq.thymeleaf</groupId>
		    <artifactId>thymeleaf-layout-dialect</artifactId>
		</dependency>


@src/main/resources/template/index.html
`layout:decorator="layout/default"`에 적힌 경로의 default.html이 이문서의 구조를 나타낸다.

    <html xmlns:th="http://www.thymeleaf.org"
          xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
          layout:decorator="layout/default">
	  

@src/main/resources/template/layout/default.html
참조하는 fragment파일
* fragments/head.html
* fragments/header.html
* fragments/footer.html

        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org"
            xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
            
        <!-- 1.headFragment로 head -->	  
        <!-- <head th:replace="fragments/head::headFragment"> -->
        <head>
          <!-- 페이지별 타이틀 부여하기: layout페이지에 작성해야함.-->
          <!-- $LAYOUT_TITLE : 전페이지 공통-->
          <!-- $CONTENT_TITLE : 페이지별 타이틀-->
          <title layout:title-pattern="$LAYOUT_TITLE :: $CONTENT_TITLE">Thymeleaf</title>
          
          
          <!-- include는 fragment태그의 자식태그만 가져옴-->
          <th:block th:include="fragments/head::headFragment"></th:block>
        </head>
        <body>
        <div id="container">
          <!-- 일반 html태그에 대하여 include/replace는 fragment태그를 제외하고 가져온다. insert는 fragment태그 포함 -->
          <header th:include="fragments/header::headerFragment"></header>
          
          <section id="content">
            <!-- 각 페이지의 컨텐츠가 삽입될 부분 -->
            <div layout:fragment="content"></div>
          </section>
          
          <footer th:replace="fragments/footer::footerFragment"></footer>
        </div>
        </body>
        </html>

@src/main/resources/template/layout/index.html
`layout:fragment="content"`의 내용이 default.html의 `<div layout:fragment="content"></div>`안에 작성되게 된다.

    <body layout:fragment="content"></body>

아래 두 api를 조회한다.
* 택배사조회api
* 운송장조회api 


          <div class="wrapper">
            <select class="custom-select" id="deliveryCompnayList" name="deliveryCompnayList"></select>
              
            <input type="text" class="form-control" id="invoiceNumberText" name="invoiceNumberText" placeholder="운송장 번호">
            <button type="button" class="btn btn-outline-success btn-lg btn-block" id="myButton1">조회</button>
            
          </div>

          <hr />
          
          <div class="tbl-wrapper">
            <table id="tbl-invoice" class="table"></table>
            <table id="tbl-tracking" class="table table-hover"></table>
          </div>

            


        <script>
        //api key 갱신하기
        //https://tracking.sweettracker.co.kr/templates/app.html#/apikey/add
        let myKey = "ElEr47kWURCxNazU3sxyLA"; // sweet tracker에서 발급받은 자신의 키 넣는다.

        $(()=>{
          //onload시에 apikey유효기간 관련 경고창 출력
          $("#exampleModalCenter").modal();
          
          
          
          // 택배사 목록 조회 company-api
            $.ajax({
                type:"GET",
                dataType : "json",
                url:"http://info.sweettracker.co.kr/api/v1/companylist",
                data: {
                  t_key : myKey
                },
                success: data => {
                    
                  var CompanyArray = data.Company; // Json Array에 접근하기 위해 Array명 Company 입력
                  console.log(CompanyArray); 
                  
                  var myData="<option>택배회사명</option>";
                  $.each(CompanyArray,function(key,value) {
                        myData += ('<option value='+value.Code+'>'+value.Name + '</option>');        				
                  });
                  
                  $("#deliveryCompnayList").html(myData);
                }
            });
          
          // 배송정보와 배송추적 tracking-api
            $("#myButton1").click(function() {
              
              var t_code = $('#deliveryCompnayList option:selected').attr('value');
              var t_invoice = $('#invoiceNumberText').val();
              
              //CJ대한통운인경우, 숫자아닌 문자제거
              if(t_code == '04'){
                t_invoice = t_invoice.replace(/\W/g, "");
                $('#invoiceNumberText').val(t_invoice);
              }
              
              
                $.ajax({
                    type:"GET",
                    dataType : "json",
                    url:"http://info.sweettracker.co.kr/api/v1/trackingInfo",
                    data: {
                      t_key: myKey,
                      t_code: t_code,
                      t_invoice: t_invoice
                    },
                    success:function(data){
                      console.log(data);
                      var myInvoiceData = "";
                      if(data.status == false){
                        myInvoiceData += ('<p>'+data.msg+'<p>');
                      }else{
                        myInvoiceData += ('<tr>');            	
                        myInvoiceData += ('<th>'+"보내는사람"+'</td>');     				
                        myInvoiceData += ('<th>'+data.senderName+'</td>');     				
                        myInvoiceData += ('</tr>');     
                        myInvoiceData += ('<tr>');            	
                        myInvoiceData += ('<th>'+"제품정보"+'</td>');     				
                        myInvoiceData += ('<th>'+data.itemName+'</td>');     				
                        myInvoiceData += ('</tr>');     
                        myInvoiceData += ('<tr>');            	
                        myInvoiceData += ('<th>'+"송장번호"+'</td>');     				
                        myInvoiceData += ('<th>'+data.invoiceNo+'</td>');     				
                        myInvoiceData += ('</tr>');     
                        myInvoiceData += ('<tr>');            	
                        myInvoiceData += ('<th>'+"송장번호"+'</td>');     				
                        myInvoiceData += ('<th>'+data.receiverAddr+'</td>');     				
                        myInvoiceData += ('</tr>');           	                		
                      }
                  
                      
                      $("#tbl-invoice").html(myInvoiceData)
                      
                      var trackingDetails = data.trackingDetails;
                      
                      
                    var header ="<thead>";
                    header += '<tr>';            	
                    header += '<th>'+"시간"+'</th>';
                    header += '<th>'+"장소"+'</th>';
                    header += '<th>'+"유형"+'</th>';
                    header += '<th>'+"전화번호"+'</th>';     				
                  header += '</tr>';     
                  header += '</thead>';     
                    
                    var myTracking = "<tbody>";
                    $.each(trackingDetails,function(key,value) {
                        myTracking += ('<tr>');            	
                      myTracking += ('<td>'+value.timeString+'</td>');
                      myTracking += ('<td>'+value.where+'</td>');
                      myTracking += ('<td>'+value.kind+'</td>');
                      myTracking += ('<td>'+value.telno+'</td>');     				
                        myTracking += ('</tr>');        			            	
                    });
                      myTracking += '</tbody>';        			            	
                    
                    $("#tbl-tracking").html(header+myTracking);
                      
                    }
                });
            });
        });

        </script>
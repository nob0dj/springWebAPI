# WebSocketWithSockAndStomp
회원관련 기능이 모두 구현되어 있는 spring legacy project에서 진행한다.
**oracle데이터베이스 쿼리는 프로젝트 root directory에 websocket.sql파일 사용할 것.**

[[Spring - WebSocket을 활용한 채팅 서비스 구현]](https://m.blog.naver.com/scw0531/221052774287)
[Spring 4.x에서의 WebSocket, SockJS, STOMP](https://netframework.tistory.com/entry/Spring-4x%EC%97%90%EC%84%9C%EC%9D%98-WebSocket-SockJS-STOMP)
[Spring에서 WebSocket 사용시 HttpSession에 저장된 값 사용하기-HttpSessoinHankshakeInterceptor](https://mobilenweb.tistory.com/174)
[Spring WebSocket 소개 - httpHandshake설명 잘되어 있음.](https://supawer0728.github.io/2018/03/30/spring-websocket/)




## 1.표준 WebSocket
Spring 4.0 환경에서부터  Spring스팩에서 Web에 속해있다. 

@pom.xml
5.0.6.RELEASE로 테스트

    <!-- 웹소켓 의존 추가 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-websocket</artifactId>
        <version>${org.springframework-version}</version>
    </dependency>


@web.xml
인코딩 필터나 앱 서블릿 부분에 `<async-supported>`를 추가해준다. 이와같이 비동기 작업이 필요한 이유는 클라이언트가 여러개일 경우 해당 클라이언트들이 동시에 데이터를 전송할 수 있기에 해당 제어를 위해서 비동기 처리한다. 만약 비동기 처리를 하지 않을 경우 1:1 통신은 에러가 나지 않지만 2개이상의 클라이언트가 접속하여 데이터를 주고받을 시 비동기 에러가 난다. 

    <!-- Processes application requests -->
	<servlet>
		<servlet-name>appServlet</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>/WEB-INF/spring/appServlet/*-context.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
		<async-supported>true</async-supported>
	</servlet>


## Handler작성

@com.kh.spring.ws.EchoHandler
스프링 웹소켓은 문자열을 다루는 TextWebSocketHandler와 이진데이터를 다루는 BinaryWebSocketHandler구현클래스로 서버측 ConnectionEndPoint를 작성한다.

* afterConnectionEstablished: 클라이언트와 연결 이후에 실행되는 메서드
* handleTextMessage: 클라이언트가 서버로 메시지를 전송했을 때 실행되는 메서드
* afterConnectionClosed: 클라이언트와 연결을 끊었을 때 실행되는 메소드

    @Component
    public class EchoHandler extends TextWebSocketHandler {
        Logger logger = LoggerFactory.getLogger(EchoHandler.class);
        List<WebSocketSession> sessionList = new ArrayList<WebSocketSession>();

        // 클라이언트와 연결 이후에 실행되는 메서드
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessionList.add(session);
            logger.info("{} 연결됨", session.getId());
        }

        // 클라이언트가 서버로 메시지를 전송했을 때 실행되는 메서드
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            //client단 기본 websocket은 
            logger.info("{}로 부터 {} 받음", session.getId(), message.getPayload());
            for (WebSocketSession sess : sessionList) {
                sess.sendMessage(new TextMessage(session.getId() + " : " + message.getPayload()));
            }
        }

        // 클라이언트와 연결을 끊었을 때 실행되는 메소드
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessionList.remove(session);
            logger.info("{} 연결 끊김", session.getId());
        }
    }

## Handler 등록
ConnectionEndPoint설정은 WebSocketConfigurer 인터페이스 구현클래스의 registerWebSocketHandlers메소드에서 한다.
* 의존받은 Handler객체
* ConnectionEndPoint url

@com.kh.spring.ws.EchoHandlerConfiguration

    @Configuration
    @EnableWebSocket
    public class EchoHandlerConfiguration implements WebSocketConfigurer {
        @Autowired
        private EchoHandler echoHandler;

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(echoHandler, "/echo");
        }
    }

만약, annotation이 아닌 선언적 방식으로 구현하고자 한다면 EchoHandlerconfiguration클래스 대신
@/WEB-INF/spring/appServlet/servlet-context.xml 파일에 빈을 관련 빈을 등록할 수 있다.

    <bean id="echoHandler" class="com.kh.spring.ws.EchoHandler" />
    <websocket:handlers>
            <websocket:mapping handler="echoHandler" path="/echo" />
    </websocket:handlers>


## client단 작성
WebSocket은 ws프로토콜을 포함한 url을 작성해야 한다.

@/WEB-INF/views/ws/websocket.js
웹소켓은 ws://프로토콜을 사용한다.

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
    let ws = new WebSocket("ws://"+window.location.host+"${pageContext.request.contextPath}/echo");
    ws.onopen = onOpen;
    ws.onmessage = onMessage;
    ws.onclose = onClose;

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
        ws.send($("#message").val());
    }
    </script>



## 2.Sock.js 사용하기
WebSocket은 지금까지 사용되고 있던 Ajax에 비하여 매우 훌륭한 방법
WebSocket의 경우, IE 10이상에서만 지원
이 문제를 해결하기 위해서, socket.io와 같은 websocket을 지원하지 않는 browser에서 websocket과 같이 접근하기 위한 방법들이 계속해서 나왔는데, SockJS도 그 방법들중 하나

SockJS는 기본적으로 WebSocket을 이용하고 있고, interface가 동일함.

차이점
* schema가 ws가 아닌 http
* WebSocket과 같이 Browser에서 제공되는 library가 아닌, 외부 library 사용
* IE 6이상부터 지원.
* Server 측에서도 websocket이 아닌 SockJS server side library를 사용


[[Spring] 웹 소켓( Web Socket ) 예제 - sock.js](https://victorydntmd.tistory.com/253)

@com.kh.spring.ws.EchoHandlerConfiguration
sockjs에 해당하는 connectionEndPoint등록.
Handler객체 등록후에, 서버측 Sockjs를 사용하겠다는 메소드 withSockJS호출

    @Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(echoHandler, "/echo");
		registry.addHandler(echoHandler, "/echo").withSockJS();//서버사이드 SockJS사용
	}

만약, annotation이 아닌 선언적 방식으로 구현하고자 한다면 EchoHandlerconfiguration클래스 대신
@/WEB-INF/spring/appServlet/servlet-context.xml 파일에 빈을 관련 빈을 등록할 수 있다.

    <!-- sockjs관련 빈등록하기 -->
	<!-- <beans:bean id="echoHandler" class="com.kh.spring.ws.EchoHandler" />
	<websocket:handlers>
		<websocket:mapping handler="echoHandler" path="/echo" />
		<websocket:sockjs />
	</websocket:handlers> -->



@/WEB-INF/views/ws/sock.jsp
sock.js는 http프로토콜을 사용한다.

    <script>

    ...


    // 웹소켓을 지정한 url로 연결한다.
    //let sock = new SockJS("<c:url value="/echo"/>");
    let sock = new SockJS("${pageContext.request.contextPath}/echo");
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
        $("#data").append(data + "<br/>");
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

테스트: sessionId값이 더이상 단순넘버링이 아니라 문자열 토큰을 발행한다.





## 3.stomp.js
subscription, user개념을 도입.

[공식api tutorial: Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)

비회원, 회원 모두 관리자와 채팅이 가능하고, 이를 db에서 관리한다.
단, 비회원은 jsessionid를 memberId로 사용하는데, 이를 client단에서 javascript를 통해 가져오지 못하므로(httpOnly 옵션 true인 경우, document.cookie로 접근 불가), 서버측에서 속성값으로 관리한다.

#### 구현내용
사용자는 관리자와 1:1채팅할 수 있다. 실시간으로 채팅내역을 확인할 수 있다.
관리자는 모든 사용자와 채팅이 가능하고, 이를 목록으로 관리한다.
* 목록은 읽지 않은 내용을 먼저 보여준후, 다 읽은 채팅내역을 보여준다.
* 사용자와 채팅은 popup창을 통해 각각 관리한다.
* 관리자는 마지막 확인 시점을 기준으로 읽지 않은 메세지를 카운트 할 수 있다.(카톡과 동일)
* 목록은 실시간으로 갱신된다.



@com.kh.spring.stomp.controller.StompController

    @GetMapping("/ws/stomp.do")
	public void websocket(Model model, HttpSession session){
		//비회원일 경우, httpSessionId값을 chatId로 사용한다. 
		//클라이언트에서는 httpOnly-true로 설정된 cookie값은 document.cookie로 가져올 수 없다.
		model.addAttribute("httpSessionId", session.getId());
	}


@/WEB-INF/views/ws/stomp.jsp
core태그 변수로 chatId 선언

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


@com.kh.spring.stomp.StompConfigurer

    @Configuration
    @EnableWebSocketMessageBroker
    public class StompConfigurer extends AbstractWebSocketMessageBrokerConfigurer{
        
        /**
        * stomp로 접속한 경우의 connectEndPoint설정: client단의 `Stomp.over(socket)`에 대응함.
        * 
        * 내부적으로 SockJS를 통해, websocket, xhr-streaming, xhr-polling 중에 가장 적합한 transport를 찾음.
        */
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/stomp")
                    .withSockJS()
                    .setInterceptors(new HttpSessionHandshakeInterceptor());
            
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            //핸들러메소드의 @SendTo 에 대응함. 여기서 등록된 url을 subscribe하는 client에게 전송.
            registry.enableSimpleBroker("/hello", "/chat");
            
            //prefix로 contextPath를 달고 @Controller의 핸들러메소드@MessageMapping 를 찾는다.
            registry.setApplicationDestinationPrefixes("/spring");//contextPath
        }
        
    }

이를 xml에 선언적으로 구성한다면, 다음과 같다
@/WEB-INF/spring/appServlet/servlet-context.xml

    <!-- stomp관련 빈등록하기 -->
	<!-- <websocket:message-broker application-destination-prefix="/spring">
		<websocket:stomp-endpoint path="/stomp">
			<websocket:handshake-interceptors>
				<beans:bean class="org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor" />
			</websocket:handshake-interceptors>
			<websocket:sockjs session-cookie-needed="true"/>
		</websocket:stomp-endpoint>
		<websocket:simple-broker prefix="/hello, /chat"/>
	</websocket:message-broker> -->






@com.kh.spring.stomp.controller.StompController
client가 `/hello`로 요청하면, `/hello`로 구독한 client에게 메세지 전송.

WebSocketSessionId 가져오기
* `@Header("simpSessionId") String sessionId`: 생략
*  SimpMessageHeaderAccessor headerAccessor를 통해서 HttpSEssion 속성값 가져오기
    `headerAccessor.getSessionId();`값의 결과은 동일하다.

결론
* 로그인하지 않았다면, cookie로 전달된 JSESSIONID값을 memberId로 사용


    @MessageMapping("/hello")
	@SendTo("/hello")
	public Msg stomp(Msg fromMessage,
					 @Header("simpSessionId") String sessionId,//WesocketSessionId값을 가져옴.
					 SimpMessageHeaderAccessor headerAccessor//HttpSessionHandshakeInterceptor빈을 통해 httpSession의 속성에 접근 가능함.
					 ){
		logger.info("fromMessage={}",fromMessage);
		logger.info("@Header sessionId={}",sessionId);
		
		//httpSession속성 가져오기
		String sessionIdFromHeaderAccessor = headerAccessor.getSessionId();//@Header sessionId와 동일
		Map<String,Object> httpSessionAttr = headerAccessor.getSessionAttributes();
		Member member = (Member)httpSessionAttr.get("memberLoggedIn");
		String httpSessionId = (String)httpSessionAttr.get("HTTP.SESSION.ID");//비회원인 경우 chatId로 사용함.
		logger.info("sessionIdFromHeaderAccessor={}",sessionIdFromHeaderAccessor);
		logger.info("httpSessionAttr={}",httpSessionAttr);
		logger.info("memberLoggedIn={}",member);
		
        return fromMessage; 
	}


### Database 구축
**CHATROOM 복합PK(chatid, memberid)를 CHATLOG테이블에서 복합FK(chatid, memberid)로 참조함.**


    CREATE TABLE "SPRING"."CHATROOM" 
    (
        "CHATID" CHAR(20 BYTE), 
        "MEMBERID" VARCHAR2(256 BYTE) NOT NULL ENABLE, 
        "LASTCHECK" NUMBER DEFAULT 0, 
        "STATUS" CHAR(1 BYTE) DEFAULT 'Y' NOT NULL ENABLE, 
        "STARTDATE" DATE DEFAULT sysdate, 
        "ENDDATE" DATE, 
        CONSTRAINT "CK_CHATROOM_STATUS" CHECK (status in('Y','N')) ENABLE, 
        CONSTRAINT "PK_CHATROOM" PRIMARY KEY ("CHATID","MEMBERID")
    );


    CREATE TABLE "SPRING"."CHATLOG"     
    (	
        "CHATNO" NUMBER, 
        "CHATID" CHAR(20 BYTE) NOT NULL, 
        "MEMBERID" VARCHAR2(256 BYTE), 
        "MSG" VARCHAR2(2000 BYTE), 
        "TIME" NUMBER NOT NULL, 
        CONSTRAINT "PK_CHATLOG" PRIMARY KEY ("CHATNO"),
        CONSTRAINT "FK_CHATID_MEMBERID" FOREIGN KEY ("CHATID","MEMBERID")
                                        REFERENCES "SPRING"."CHATROOM" ("CHATID","MEMBERID")
    );

    CREATE SEQUENCE SEQ_CHATLOG;


    
## VO클래스

@com.kh.spring.stomp.model.vo.ChatRoom

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public class ChatRoom {
        
        private String chatId;
        private String memberId;
        private long lastCheck;
        private String status;
        private Date startDate;
        private Date endDate;

    }

@com.kh.spring.stomp.model.vo.Msg

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public class Msg {
        private long chatNo;
        private String chatId;
        private String memberId;
        private String msg;
        private long time;
        private MsgType type;
    }

@com.kh.spring.stomp.model.vo.MsgType
메세지타입을 관리하기 위한 enum형. 프로젝트에서는 MESSAGE만 사용하였다.

    public enum MsgType {
        INIT, MESSAGE, FILE
    }



## chatroom테이블 데이터 생성 및 조회

@com.kh.spring.stomp.controller.StompController

    @Autowired
	StompService chatService;
	
	@GetMapping("/ws/stomp.do")
	public void websocket(Model model, 
						  HttpSession session, 
						  @SessionAttribute(value="memberLoggedIn", required=false) Member memberLoggedIn){
		String memberId = Optional.ofNullable(memberLoggedIn).map(Member::getMemberId).orElse(session.getId());
		String chatId = null;
		
		//chatId조회
		//1.memberId로 등록한 chatroom존재여부 검사. 있는 경우 chatId 리턴.
		chatId = stompService.findChatIdByMemberId(memberId);
		
		//2.로그인을 하지 않았거나, 로그인을 해도 최초접속인 경우 chatId를 발급하고 db에 저장한다.
		if(chatId == null){
			chatId = getRandomChatId(15);//chat_randomToken -> jdbcType=char(20byte)
			
			List<ChatRoom> list = new ArrayList<>();
			list.add(new ChatRoom(chatId, "admin", 0, "Y", null, null));
			list.add(new ChatRoom(chatId, memberId, 0, "Y", null, null));
			stompService.insertChatRoom(list);
		}
		
		logger.info("memberId=[{}], chatId=[{}]",memberId, chatId);
		
		
		//비회원일 경우, httpSessionId값을 memberId로 사용한다. 
		//클라이언트에서는 httpOnly-true로 설정된 cookie값은 document.cookie로 가져올 수 없다.
		model.addAttribute("memberId", memberId);
		model.addAttribute("chatId", chatId);
	}


## findChatIdByMemberId구현

@src/main/resources/mapper/stomp/stomp-mapper.xml

    <select id="findChatIdByMemberId" resultType="string">
		SELECT CHATID FROM CHATROOM 
		WHERE STATUS = 'Y'
			AND MEMBERID = #{memberId}
	</select>



## insertChatRoom(List<ChatRoom>)구현
* 사용자별 lastCheck컬럼 정보를 관리하기 위해서 chatId+memberId를 PK로 삼고, 마지막확인시간을 long타입으로 관리한다.
* 트랜잭션 처리할 것

@com.kh.spring.stomp.model.service.StompServiceImpl

    @Service
    @Transactional(rollbackFor=Exception.class)
    public class StompServiceImpl implements StompService {

        @Autowired
        StompDao stompDao;

        @Override
        public String findChatIdByMemberId(String memberId) {
            return stompDao.findChatIdByMemberId(memberId);
        }

        @Override
        public int insertChatRoom(List<ChatRoom> list) {
            int result = 0;
            for(ChatRoom chatRoom: list){
                result += stompDao.insertChatRoom(chatRoom);
            }
            return result;
        }
    }

@src/main/resources/mapper/stomp/stomp-mapper.xml

    <insert id="insertChatRoom">
		INSERT INTO CHATROOM (CHATID, MEMBERID)
		VALUES(#{chatId}, #{memberId})
	</insert>


## updateLastCheck 구현
관리자의 안읽음 메세지 카운트를 관리하기위해서 마지막 확인 시각을 db chatroom테이블 lastcheck컬럼에 기록/관리한다. 

@/WEB-INF/views/ws/stomp.jsp
    
    <script type="text/javascript">
    $(document).ready(function() {
       ...

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


@com.kh.spring.stomp.controller.StompController

    @MessageMapping("/lastCheck")
	public void lastCheck(@RequestBody Msg fromMessage){
		logger.info("fromMessage={}",fromMessage);
		
		stompService.updateLastCheck(fromMessage);
	}
	

@src/main/resources/mapper/stomp/stomp-mapper.xml
    
    <update id="updateLastCheck">
		UPDATE CHATROOM SET LASTCHECK =  #{time}
		WHERE CHATID = #{chatId} AND MEMBERID = #{memberId}
	</update>


## insertChatLog 구현

@/WEB-INF/views/ws/stomp.jsp

    function sendMessage() {
        let data = {
            chatId : "${chatId}",
            memberId : "${memberId}",
            msg : $("#message").val(),
            time : new Date().getTime(),
            type: "MESSAGE"
        }
        stompClient.send('<c:url value="/chat/${chatId}" />', {}, JSON.stringify(data));
    }


@src/main/resources/mapper/stomp/stomp-mapper.xml

    <insert id="insertChatLog">
		INSERT INTO CHATLOG (CHATNO, CHATID, MEMBERID, MSG, TIME)
		VALUES(SEQ_CHATLOG.NEXTVAL, #{chatId}, #{memberId}, #{msg}, #{time})
	</insert>



## 관리자 채팅목록 페이지: findRecentList 구현

@src/main/resources/mapper/stomp/stomp-mapper.xml
mapper.xml에서는 --주석이 에러유발하므로, 복붙할 때 주의할 것.

    <select id="findRecentList" resultType="map">
    --안읽은 메세지 개수 및 마지막 메세지 조회
    select *
    from (
        select chatno,
            A.chatid, 
            (select memberid from chatroom where A.chatid = chatid and memberid != 'admin') memberid, 
            msg, 
            time,
            count(*) over(partition by A.chatid,A.memberid) cnt,
            rank() over(partition by A.chatid order by time desc) rank --chatid,memberid별 가장 최근 msg를 구하기위한 rank
        from chatlog A left join chatroom B
            on A.chatid = B.chatid and A.memberid = B.memberid
        --채팅방의 상대 admin의 lastcheck와 비교해서 이후에 쓰인 메세지만 필터링한다.
        where time > (select lastcheck from chatroom C where C.chatid = A.chatid and memberid = 'admin')
        order by time desc)A
    where rank = 1
    union all --조회한 순서 유지를 위해서 union all
    --모두 읽은 채팅방 조회
    select *
    from (
        select chatno,
            A.chatid, 
            (select memberid from chatroom where A.chatid = chatid and memberid != 'admin') memberid, --마지막 메세지가 관리자인 경우
            msg, 
            time,
            0 cnt,
            rank() over(partition by A.chatid order by time desc) rank --chatid,memberid별 가장 최근 msg를 구하기위한 rank
        from chatlog A left join chatroom B
            on A.chatid = B.chatid and A.memberid = B.memberid
        order by time desc) A
    where rank = 1 and time <= (select lastcheck from chatroom C where C.chatid = A.chatid and memberid = 'admin');
    </select>


@com.kh.spring.stomp.controller.AdminStompController

    @GetMapping("/ws/admin.do")
	public void admin(Model model, 
                      HttpSession session, 
                      @SessionAttribute(value="memberLoggedIn", required=false) Member memberLoggedIn){
		String memberId = Optional.ofNullable(memberLoggedIn).map(Member::getMemberId).orElse(session.getId());
		String chatId = null;
		
		List<Map<String, String>> recentList = stompService.findRecentList();
		logger.info("recentList={}",recentList);
		
		model.addAttribute("recentList", recentList);
		
	}



@com.kh.spring.stomp.controller.StompController
관리자페이지는 `/chat/admin`를 구독하면서, 사용자 메세지가 있을경우 목록을 최신화한다.

    @MessageMapping("/chat/{chatId}")
	@SendTo(value={"/chat/{chatId}", "/chat/admin"})
	public Msg sendEcho(Msg fromMessage, 
						@DestinationVariable String chatId, 
						@Header("simpSessionId") String sessionId){
                            ...
    }
	
	@MessageMapping("/lastCheck")
	@SendTo(value={"/chat/admin"})
	public Msg lastCheck(@RequestBody Msg fromMessage){
        ...
    }

@/WEB-INF/views/ws/admin.jsp
    
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

        });

    });

    function goChat(chatId){
        open("${pageContext.request.contextPath}/ws/adminChat.do/"+chatId, chatId, "width=500, height=500", false);
    }
    </script>

## 팝업창구현


팝업창으로 다른 회원과 채팅하게 된다.

    @GetMapping("/ws/adminChat.do/{chatId}")
	public String adminChat(@PathVariable("chatId") String chatId){
		return "ws/adminChat";
	}    


@/WEB-INF/views/ws/adminChat.jsp

    <input type="text" id="message" />
    <input type="button" id="sendBtn" value="전송" />
    <div id="data"></div>
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

        // subscribe message
        stompClient.subscribe('/chat/${chatId}', function(message) {
            console.log("receive from /chat/${chatId} :", message);
            let messsageBody = JSON.parse(message.body);
            $("#data").append(messsageBody.memberId+":"+messsageBody.msg+ "<br/>");
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

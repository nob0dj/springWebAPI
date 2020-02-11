package com.kh.spring.stomp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kh.spring.member.model.vo.Member;
import com.kh.spring.stomp.model.service.StompService;
import com.kh.spring.stomp.model.vo.ChatRoom;
import com.kh.spring.stomp.model.vo.Msg;



@Controller
public class StompController {
		
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	StompService stompService;
	
	@GetMapping("/ws/stomp.do")
	public void websocket(Model model, 
						  HttpSession session, 
						  @SessionAttribute(value="memberLoggedIn", required=false) Member memberLoggedIn){
		
		//비회원일 경우, httpSessionId값을 memberId로 사용한다.
		String memberId = Optional.ofNullable(memberLoggedIn)
								  .map(Member::getMemberId)
								  .orElse(session.getId());//HttpSession의 JSESSIONID값을 저장
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
		//chatId가 존재하는 경우, 채팅내역 조회
		else{
			List<Msg> chatList = stompService.findChatListByChatId(chatId);
			model.addAttribute("chatList", chatList);
		}
		
		logger.info("memberId=[{}], chatId=[{}]",memberId, chatId);
		
		model.addAttribute("memberId", memberId);
		model.addAttribute("chatId", chatId);
	}
	
	/**
	 * 인자로 전달될 길이의 임의의 문자열을 생성하는 메소드
	 * 영대소문자/숫자의 혼합.
	 * 
	 * @param len
	 * @return
	 */
	private String getRandomChatId(int len){
		Random rnd = new Random();
		StringBuffer buf =new StringBuffer();
		buf.append("chat_");
		for(int i=0;i<len;i++){
			//임의의 참거짓에 따라 참=>영대소문자, 거짓=> 숫자
		    if(rnd.nextBoolean()){
		    	boolean isCap = rnd.nextBoolean();
		        buf.append((char)((int)(rnd.nextInt(26))+(isCap?65:97)));
		    }
		    else{
		        buf.append((rnd.nextInt(10))); 
		    }
		}
		return buf.toString();

	}
	
	/**
	 * - @MessageMapping 을 통해 메세지를 받고,
	 * - @SendTo 를 통해 메세지 전달. 작성된 주소를 구독하고 있는 client에게 메세지 전송
	 * 
	 * @param fromMessage
	 * @return
	 */
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
		String httpSessionId = (String)httpSessionAttr.get("HTTP.SESSION.ID");//비회원인 경우 memberId로 사용함.
		logger.info("sessionIdFromHeaderAccessor={}",sessionIdFromHeaderAccessor);
		logger.info("httpSessionAttr={}",httpSessionAttr);
		logger.info("memberLoggedIn={}",member);
		
		return fromMessage; 
	}
	
	@MessageMapping("/chat/{chatId}")
	@SendTo(value={"/chat/{chatId}", "/chat/admin"})
	public Msg sendEcho(Msg fromMessage, 
						@DestinationVariable String chatId, 
						@Header("simpSessionId") String sessionId){
		logger.info("fromMessage={}",fromMessage);
		logger.info("chatId={}",chatId);
		logger.info("sessionId={}",sessionId);
		
		stompService.insertChatLog(fromMessage);

		return fromMessage; 
	}
	/**
	 * 읽음 여부 확인을 위해 최종 focus된 시각정보를 수집한다.
	 * 
	 * @param fromMessage
	 * @return
	 */
	@MessageMapping("/lastCheck")
	@SendTo(value={"/chat/admin"})
	public Msg lastCheck(@RequestBody Msg fromMessage){
		logger.info("fromMessage={}",fromMessage);
		
		stompService.updateLastCheck(fromMessage);
		
		return fromMessage; 
	}
	
}

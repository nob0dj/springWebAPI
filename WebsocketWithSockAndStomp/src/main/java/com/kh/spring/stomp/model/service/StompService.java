package com.kh.spring.stomp.model.service;

import java.util.List;
import java.util.Map;

import com.kh.spring.stomp.model.vo.ChatRoom;
import com.kh.spring.stomp.model.vo.Msg;

public interface StompService {

	String findChatIdByMemberId(String memberId);

	int insertChatRoom(List<ChatRoom> list);

	int insertChatLog(Msg fromMessage);

	int deleteChatRoom(String chatId);

	int updateLastCheck(Msg fromMessage);

	
	//관리자용
	List<Map<String, String>> findRecentList();

	List<Msg> findChatListByChatId(String chatId);

}

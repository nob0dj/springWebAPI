package com.kh.spring.stomp.model.dao;

import java.util.List;
import java.util.Map;

import com.kh.spring.stomp.model.vo.ChatRoom;
import com.kh.spring.stomp.model.vo.Msg;

public interface StompDao {

	String findChatIdByMemberId(String memberId);

	int insertChatRoom(ChatRoom chatRoom);

	int insertChatLog(Msg fromMessage);

	int deleteChatRoom(String chatId);

	int updateLastCheck(Msg fromMessage);

	//관리자용
	List<Map<String, String>> findRecentList();

	List<Msg> findChatListByChatId(String chatId);

}

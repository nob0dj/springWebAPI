package com.kh.spring.stomp.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.stomp.model.dao.StompDao;
import com.kh.spring.stomp.model.vo.ChatRoom;
import com.kh.spring.stomp.model.vo.Msg;



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
	
	@Override
	public int updateLastCheck(Msg fromMessage) {
		return stompDao.updateLastCheck(fromMessage);
	}

	@Override
	public int insertChatLog(Msg fromMessage) {
		//메세지 입력시 lastCheck컬럼값도 갱신
		updateLastCheck(fromMessage);
		return stompDao.insertChatLog(fromMessage);
	}

	@Override
	public int deleteChatRoom(String chatId) {
		return stompDao.deleteChatRoom(chatId);
	}

	@Override
	public List<Map<String, String>> findRecentList() {
		return stompDao.findRecentList();
	}

	@Override
	public List<Msg> findChatListByChatId(String chatId) {
		return stompDao.findChatListByChatId(chatId);
	}

	
}

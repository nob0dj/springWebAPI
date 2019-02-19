package com.kh.spring.stomp.model.vo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

package com.kh.spring.summernote.model.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Summernote extends AbstractPersistable<Long> implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * AbstractPersistable을 상속했으므로 id필드를 생략해도 되지만, 
	 * command객체 필드 바인딩을 위해서 명시함.
	 * 생략한 경우, update에서 id값이 누락된다.
	 * 
	 */
	@Id
	Long id;
	
	
	@NonNull
	String writer;
	
	@NonNull
	String contents;
	
	/*input:date => java.util.Date타입으로  자동변환을 위해 포맷지정*/
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Temporal(TemporalType.DATE)
	Date regDate;

}

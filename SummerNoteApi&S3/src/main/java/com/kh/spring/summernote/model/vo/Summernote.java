package com.kh.spring.summernote.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

import com.kh.spring.aws.model.vo.S3Object;

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
	 * 생략한 경우, 사용자입력값 id가 커맨드객체 id에 대입되지 않는다.
	 * 
	 */
	@Id
	Long id;
	
	
	@NonNull
	String writer;
	
	@NonNull
	String contents;
	
	/*input:date => java.util.Date타입으로  자동변환을 위해 포맷지정*/
//	@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
//	@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)
	@DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	/* 날짜/시각 정보를 모두 지정하기 위해 아래 어노테이션을 사용한다.(기본값이므로 생략가능. DATE, TIME을 사용하면 해당정보만 저장됨)*/
	@Temporal(TemporalType.TIMESTAMP)
	Date regDate = new Date();

	/**
	 * db column is_temp char(1) 'Y','N' default 'N'
	 * entity attr isTemp:Boolean
	 */
//	@Convert(converter=BooleanToStringConverter.class)
	@Type(type="yes_no")
	@Column(name="is_temp")
    private Boolean tempFlag = false;  	
	
	/**
	 * mappedBy속성을 통해 읽기 전용으로 처리함.
	 * mappedBy속성이 없으면, summernote_file_list 테이블을 찾는다
	 */
	@OneToMany(mappedBy="summernote")
	private List<S3Object> fileList;
}

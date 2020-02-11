package com.kh.spring.aws.model.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.kh.spring.summernote.model.vo.Summernote;

import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude="summernote")//StackOverFlowError유발하므로, 반드시 해당필드 제외할 것
public class S3Object extends AbstractPersistable<Long> implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 기본적으로 db컬럼명을 찾을 때, originalfilename 또는 camelCasing을 기준으로 original_file_name컬럼을 찾는다.
	 */
	@Column(name="original_filename")
    String originalFileName;
    
    @Column(name="renamed_filename")
    String renamedFileName;
    
    @Column
    String resourceUrl;
    
    @Column
    String contentType;
    
    /**
     * oracle에서 size는 예약어라 컬럼명으로 사용할 수 없다.
     */
    @Column(name="file_size")
    long size;
    
    @Column
    long downloadCount;
    
    @Column
    Date regDate;
    
    
    @ManyToOne
    Summernote summernote;
}
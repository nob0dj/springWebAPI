package com.kh.spring.aws.model.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Data;

@Entity
@Data
public class S3Object extends AbstractPersistable<Long> implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column
    String originalFileName;
    
    @Column
    String renamedFileName;
    
    @Column
    String resourceUrl;
    
    @Column
    String contentType;
    
    @Column
    long size;
    
    Date regDate;
}
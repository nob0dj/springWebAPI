package com.kh.spring.aws.model.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kh.spring.aws.model.vo.S3Object;

public interface AWSService {

	S3Object store(String saveDirectory, Long id, MultipartFile file);
	S3Object storeWithoutTempFile(String saveDirectory, Long id, MultipartFile file);

	Resource loadFileAsResource(String summernoteId, String renamedFileName);

	List<S3ObjectSummary> findAll();

	void deleteObject(String[] s3keys);
	
	void deleteBySummernoteId(Long id);


}

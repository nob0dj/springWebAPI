package com.kh.spring.aws.model.service;

import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.aws.model.vo.S3Object;

public interface AWSService {

	S3Object store(String saveDirectory, MultipartFile file);

}

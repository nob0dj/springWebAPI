package com.kh.spring.aws.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.spring.aws.model.vo.S3Object;

public interface AWSRepository extends JpaRepository<S3Object, Long> {

	void deleteBySummernoteId(Long id);

}

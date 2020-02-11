package com.kh.spring.summernote.model.service;

import java.util.List;
import java.util.Optional;

import com.kh.spring.aws.model.vo.S3Object;
import com.kh.spring.summernote.model.vo.Summernote;

public interface SummernoteService {

	Summernote save(Summernote note);

	Optional<Summernote> findById(Long id);

	void deleteById(Long id);

	List<Summernote> findAll();



}

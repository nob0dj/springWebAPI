package com.kh.spring.summernote.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.spring.summernote.model.vo.Summernote;

public interface SummernoteRepository extends JpaRepository<Summernote, Long> {

	List<Summernote> findAllByOrderByIdDesc();

}

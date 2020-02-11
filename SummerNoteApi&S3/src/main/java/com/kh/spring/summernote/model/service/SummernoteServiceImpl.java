package com.kh.spring.summernote.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.aws.model.vo.S3Object;
import com.kh.spring.summernote.model.repository.SummernoteRepository;
import com.kh.spring.summernote.model.vo.Summernote;

@Service
public class SummernoteServiceImpl implements SummernoteService {

	@Autowired
	SummernoteRepository summernoteRepository;

	@Override
	public Summernote save(Summernote note) {
		return summernoteRepository.save(note);
	}

	@Override
	public Optional<Summernote> findById(Long id) {
		return summernoteRepository.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		summernoteRepository.deleteById(id);
	}

	@Override
	public List<Summernote> findAll() {
//		return summernoteRepository.findAll(new Sort(Sort.Direction.DESC, "id"));
		return summernoteRepository.findAllByOrderByIdDesc();
	}


}

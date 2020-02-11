package com.kh.spring.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class Utils {
	
	static final Logger logger = LoggerFactory.getLogger(Utils.class);

	public static String getRenamedFileName(MultipartFile file) {
		
		String originalFileName = file.getOriginalFilename();
		String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
		int rndNum = (int)(Math.random()*1000);
		String renamedFileName = sdf.format(new Date(System.currentTimeMillis()))+"_"+rndNum+ext;
		
		logger.debug("생성된 파일명 = {} =>  {}", originalFileName, renamedFileName);
		
		return renamedFileName;
	}
}

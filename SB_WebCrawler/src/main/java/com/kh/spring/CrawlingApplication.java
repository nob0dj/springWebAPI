package com.kh.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrawlingApplication {

	public static void main(String[] args) {
		//HeadlessCrawler : 본인컴퓨터에 다운로드 받은 phantomjs실행파일의 경로를 시스템 속성으로 설정해서 ghostDriver에서 참조하도록 한다. 
		//System.setProperty("phantomjs.binary.path", "C:\\dev\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
		//상대주소로 입력시
		System.setProperty("phantomjs.binary.path", "phantomjs-2.1.1-windows/bin/phantomjs.exe");
		
		SpringApplication.run(CrawlingApplication.class, args);
	}
}

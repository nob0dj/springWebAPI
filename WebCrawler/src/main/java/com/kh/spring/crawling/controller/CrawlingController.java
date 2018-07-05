package com.kh.spring.crawling.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kh.spring.crawling.model.crawling.JavaWebCrawler;

@Controller
public class CrawlingController {
	
	@Autowired
	JavaWebCrawler javaWebCrawler;
	
	@RequestMapping("/crawling")
	public String crawler(Model model){
		List<Map<String,String>> data = null;
		try {
			data = javaWebCrawler.test("http://www.7-eleven.co.kr/event/eventList.asp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		model.addAttribute("sevenElevenEventList", data);
		
		return "crawling/crawling";
	}
}

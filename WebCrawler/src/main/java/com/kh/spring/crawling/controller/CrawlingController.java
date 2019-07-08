package com.kh.spring.crawling.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kh.spring.crawling.model.crawler.HeadlessCrawler;
import com.kh.spring.crawling.model.crawler.JavaWebCrawler;

@Controller
public class CrawlingController {
	
	@Autowired
	JavaWebCrawler javaWebCrawler;//정적페이지용 크롤러
	
	@Autowired
	HeadlessCrawler headlessCrawler;//동적으로 생성된 요소가 있는 페이지용 크롤러
	
	@RequestMapping("/crawling/7-eleven")
	public void crawlingTo7Eleven(Model model){
		List<Map<String,String>> data = null;
		try {
			String url = "http://www.7-eleven.co.kr/event/eventList.asp";
			//1.apache+jsoup
			//data = javaWebCrawler.getEventInfoByApache(url);
			//2.jsoup
			data = javaWebCrawler.getEventInfoByJSoup(url);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		model.addAttribute("data", data);
		
	}
	
	@RequestMapping("/crawling/gs25")
	public void crawlingToGS25(Model model){
		
		String url = "http://gs25.gsretail.com/gscvs/ko/customer-engagement/event/current-events";
		List<Map<String,String>> data = headlessCrawler.getEventInfo(url);
		
		model.addAttribute("data", data);

	}
}

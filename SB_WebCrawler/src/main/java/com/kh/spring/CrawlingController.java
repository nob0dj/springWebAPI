package com.kh.spring;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CrawlingController {
	
	@Autowired
	JavaWebCrawler javaWebCrawler;
	
	@Autowired
	HeadlessCrawler headlessCrawler;
	
	@RequestMapping("/crawling/7-eleven")
	@ResponseBody
	public List<Map<String,String>> crawlingTo7Eleven(){
		List<Map<String,String>> data = null;
		try {
			String url = "http://www.7-eleven.co.kr/event/eventList.asp";
			data = javaWebCrawler.test(url);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	@RequestMapping("/crawling/gs25")
	@ResponseBody
	public List<Map<String,String>> crawlingToGS25(){
		
		String url = "http://gs25.gsretail.com/gscvs/ko/customer-engagement/event/current-events";
		List<Map<String,String>> data = headlessCrawler.getEventInfo(url);
		
		return data;

	}
}

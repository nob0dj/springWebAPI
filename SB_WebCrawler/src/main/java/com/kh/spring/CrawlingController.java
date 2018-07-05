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
	
	@RequestMapping("/crawler")
	@ResponseBody
	public List<Map<String,String>> crawler(){
		List<Map<String,String>> data = null;
		try {
			data = javaWebCrawler.test("http://www.7-eleven.co.kr/event/eventList.asp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
}

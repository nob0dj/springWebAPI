package com.kh.delivery.track;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeliveryTrackController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@GetMapping("/")
	public String index(Model model) {
		logger.debug("{}", "[/] : index페이지 요청!");
		model.addAttribute("pageTitle", "스마트택배조회");
		
		Date apiKeyStart = new Date(new GregorianCalendar(2020, 0, 9).getTimeInMillis());
		Date apiKeyEnd = new Date(new GregorianCalendar(2020, 1, 9).getTimeInMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		
		model.addAttribute("apiKeyStart", dateFormat.format(apiKeyStart));
		model.addAttribute("apiKeyEnd", dateFormat.format(apiKeyEnd));
		model.addAttribute("now", dateFormat.format(new Date()));
		
		return "index";
	}
}

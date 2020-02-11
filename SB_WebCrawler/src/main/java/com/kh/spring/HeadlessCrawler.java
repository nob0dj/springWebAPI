package com.kh.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HeadlessCrawler {
  
  Logger logger = LoggerFactory.getLogger(getClass());
	
  private WebDriver driver;
  protected static DesiredCapabilities dCaps;

  public HeadlessCrawler() {
	  
	  dCaps = new DesiredCapabilities();
	  dCaps.setJavascriptEnabled(true);
	  dCaps.setCapability("takesScreenshot", false);
	  
	  driver = new PhantomJSDriver(dCaps);
	  driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }


  public List<Map<String,String>> getEventInfo(String url) {
	  	List<Map<String,String>> list = new ArrayList<>();
	    driver.get(url);
	    
	    //이벤트정보가 담기 table의 클래스명 tbl_ltype1으로 조회함. 
	    WebElement eventTable = driver.findElement(By.className("tbl_ltype1"));
	    WebElement tbody = eventTable.findElement(By.tagName("tbody"));
	    List<WebElement> trs = tbody.findElements(By.tagName("tr"));
	    
	    System.out.println("trs.size() = "+trs.size());
	    
	    for(int i = 0; i<trs.size();i++){
	    	//tr>td>a+img
	    	WebElement tr = trs.get(i);
	    	WebElement a = tr.findElement(By.cssSelector("td a"));
	    	WebElement img = tr.findElement(By.cssSelector("td img"));
	    	Map<String, String> map = new HashMap<>();
	    	
	    	map.put("href", a.getAttribute("href"));//링크
	    	map.put("eventTitle", a.getAttribute("data-dimension-value"));//이벤트명
	    	map.put("img.src", img.getAttribute("src"));//이미지
	    	list.add(map);
	    }
	    
	    System.out.println(list);
	    return  list;
  }

  public List<Map<String,String>> getTicketInfo(String url) {
	  logger.info("getTicketInfo({})",url);
	  
	  List<Map<String,String>> list = new ArrayList<>();
	  driver.get(url);
	  
	  logger.info("driver={}", driver);
	  logger.info("{}",driver.getPageSource());
	  
	  //이벤트정보가 담기 table의 클래스명 tbl_ltype1으로 조회함. 
	  
	  WebElement parent = driver.findElement(By.cssSelector("#target_ranking1 ul"));
	  List<WebElement> children = parent.findElements(By.tagName("li"));
	  
	  logger.info("li개수={}", children.size());
	  for(int i = 0; i<children.size();i++){
		  Map<String, String> map = new HashMap<>();		  
		  map.put("a", children.get(i).toString());//이미지
		  list.add(map);
	  }
	  
	  System.out.println(list);
	  return  list;
  }


}
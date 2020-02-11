package com.kh.spring;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumLoginTest {
	 
    public static void main(String[] args) {
 
    	SeleniumLoginTest selTest = new SeleniumLoginTest();
        selTest.crawl();
        
    }
 
    
    //WebDriver
    private WebDriver driver;
    
    private WebElement webElement;
    
    //Properties
    public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static final String WEB_DRIVER_PATH = "C:\\dev\\selenium\\chrome76\\chromedriver.exe";
    
    //크롤링 할 URL
    private String base_url;
    
	public SeleniumLoginTest() {
		super();

		// System Property SetUp
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

		// Driver SetUp
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		driver = new ChromeDriver(options);

		base_url = "https://nid.naver.com/nidlogin.login?mode=form&url=https%3A%2F%2Fwww.naver.com";

	}
 
    public void crawl() {
 
        try {
            //get page (= 브라우저에서 url을 주소창에 넣은 후 request 한 것과 같다)
            driver.get(base_url);
            Thread.sleep(500);
            
            //iframe 내부에서 id 필드 탐색
            webElement = driver.findElement(By.id("id"));
            String NAVER_ID = "dongxuan09";
            webElement.sendKeys(NAVER_ID);
            
            webElement = driver.findElement(By.id("pw"));
            String NAVER_PW ="6302SNi1qp9cV4";
            webElement.sendKeys(NAVER_PW);
 
            //로그인 버튼 클릭
//            webElement = driver.findElement(By.id("frmNIDLogin"));
//            webElement.submit();
            webElement = driver.findElement(By.xpath("//*[@id=\"frmNIDLogin\"]/fieldset/input"));
            webElement.click();
            
            Thread.sleep(20000);
            
        } catch (Exception e) {
            
            e.printStackTrace();
        
        } finally {
 
            driver.close();
        }
 
    }
 
}
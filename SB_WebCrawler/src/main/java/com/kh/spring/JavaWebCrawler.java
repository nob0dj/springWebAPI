package com.kh.spring;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class JavaWebCrawler {

	

	public static String getCurrentData(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        return sdf.format(new Date());

	}
	
	

	

	public List<Map<String,String>> test(String url) throws ClientProtocolException, IOException {
		String result = "";

		//start timer
		System.out.println(" Start Timer : " + getCurrentData());
		
		/*
		//#1. apache 라이브러리를 사용
		// 1. 가져올 HTTP 주소 세팅
	    HttpPost http = new HttpPost(url);
	    // 2. 가져오기를 실행할 클라이언트 객체 생성
	    HttpClient httpClient = HttpClientBuilder.create().build();
	    // 3. 실행 및 실행 데이터를 Response 객체에 담음
	    HttpResponse response = httpClient.execute(http);
	    // 4. Response 받은 데이터 중, DOM 데이터를 가져와 Entity에 담음
	    HttpEntity entity = response.getEntity();
	    // 5. Charset을 알아내기 위해 DOM의 컨텐트 타입을 가져와 담고 Charset을 가져옴 
	    ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset();
        // 6. DOM 데이터를 한 줄씩 읽기 위해 Reader에 담음 (InputStream / Buffered 중 선택은 개인취향) 
	    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
	    // 7. 가져온 DOM 데이터를 담기위한 그릇
	    StringBuffer sb = new StringBuffer();
	    // 8. DOM 데이터 가져오기
	    String line = "";
	    while((line=br.readLine()) != null){
	    	sb.append(line+"\n");
	    }
	    // 9. 가져온 아름다운 DOM을 보자
	    result  = sb.toString();
	    //System.out.println(result);
		// 10. Jsoup으로 파싱해보자.
		//Document doc = Jsoup.parse(sb.toString());
	    */
	    
		//#2.Jsoup라이브러리 사용
	    Document doc = Jsoup.connect(url).get();;
	    result = doc.data();
	    
	    //특정엘레먼트 찾기
	    Elements elems = doc.select("ul#listUl li");
//	    System.out.println("elems="+elems);
//	    System.out.println("elems="+elems.size());
	    List<Map<String,String>> eventList = new ArrayList<>();
	    
	    //elems의 마지막요소는 +(More)이라 건너뜀.
	    for(int i=0; i<elems.size()-1; i++){
	    	Element e = elems.get(i);
	    	Element a = e.select("a").get(0);
	    	//System.out.println("a="+a);
	    	Element dl = e.select("div.event_over").get(0).select("dl").get(0);
	    	//System.out.println("div="+div);
	    	
	    	Map<String,String> map = new HashMap<>();
	    	map.put("a.href", a.attr("href"));
	    	map.put("img.src", a.select("img").get(0).attr("src"));
	    	map.put("dt", dl.select("dt").get(0).text());
	    	map.put("dd", dl.select("dd.date").get(0).text());
	    	eventList.add(map);
	    }
	    System.out.println("eventList="+eventList);

	    // End Timer
	    System.out.println(" End Timer : " + getCurrentData());
	    
	    return eventList;
	}

}




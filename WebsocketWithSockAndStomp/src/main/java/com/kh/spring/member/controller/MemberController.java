package com.kh.spring.member.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.spring.member.model.exception.MemberException;
import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.vo.Member;

@SessionAttributes(value={"memberLoggedIn"})
@Controller
public class MemberController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@RequestMapping("/member/memberEnroll.do")
	public String memberEnroll(){
		if(logger.isDebugEnabled()) logger.debug("회원등록페이지!");
		//return "member/memberEnroll";
		return "member/memberEnrollWithIdCheck";
	}
	
	@RequestMapping("/member/memberEnrollEnd.do")
	public String memberEnrollEnd(@ModelAttribute("m") Member member, Model model){
		if(logger.isDebugEnabled()) logger.debug("회원등록처리페이지!");
		
		logger.debug(member.toString());
		
		String rawPassword = member.getPassword();
		System.out.println("password암호화전 : "+rawPassword);
		
		/******* password 암호화 로직 시작 *******/
		//salt값 없이 sha256 암호화 하는 경우.
		//String org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder.encodePassword(String rawPass, Object salt)
//		member.setPassword(shaPasswordEncoder.encodePassword(rawPassword, null));
		//두번째 인자 salt값으로 [id+비밀번호]로 사용함.
//		member.setPassword(shaPasswordEncoder.encodePassword(rawPassword, member.getMemberId()+rawPassword));
		
		//[[2]] : BCryptPasswordEncoder - random salt값 이용. 
		member.setPassword(bcryptPasswordEncoder.encode(rawPassword));
		//$2a$10$73Y8NPMncJ5SMB6YqtpU2eRPH2I6d/W.Qqy4SM.osF8TmjWxrH3ae
		//$2a$10$ : 알고리즘, 알고리즘 옵션
		//73Y8NPMncJ5SMB6YqtpU2eRPH2I6d/W : salt
		//Qqy4SM.osF8TmjWxrH3ae : hashedPassword
		/******* password 암호화 로직  끝 *******/
		
		
		System.out.println("password암호화후 : "+member.getPassword());
		
		
		
		//1.비지니스로직 실행
		int result = memberService.insertMember(member);
		
		//2.처리결과에 따라 view단 분기처리
		String loc = "/"; 
		String msg = "";
		if(result>0) msg="회원가입성공!";
		else msg="회원가입성공!";
		
		model.addAttribute("loc", loc);
		model.addAttribute("msg", msg);
		
		return "common/msg";
	}
	
	/*@RequestMapping(value="/member/memberLogin.do", method=RequestMethod.POST)
	public String memberLogin(@RequestParam String memberId, 
							  @RequestParam String password, 
							  Model model,
							  HttpSession session){
	
		//1.업무로직
		//random salt값으로 암호화하는 BCrypt 방식에서의 로그인 체크
		Member m = memberService.selectOneMember(memberId);
		
		 //3.view단 처리
	    String msg = "";
	    String loc = "/";
	    if(m == null){
	        msg = "존재하지 않는 아이디입니다.";			
	    }
	    else{
	        //boolean org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.matches(CharSequence rawPassword, String encodedPassword)
	        if(bcryptPasswordEncoder.matches(password, m.getPassword())){
	            msg = "로그인 성공!";
	            //세션에 로그인한 Member객체 등록
                session.setAttribute("memberLoggedIn", m);
	        }
	        else{
	            msg = "비밀번호가 틀렸습니다.";
	        }
	    }
	    
	    model.addAttribute("msg", msg);
	    model.addAttribute("loc", loc);
		
		return "common/msg";
	}*/
	
	/**
	 * session객체 이용하지 않고, model을 통해 session에 등록하기
	 * 
	 * @param memberId
	 * @param password
	 * @param model
	 * @param session
	 * @return
	 */
	/*@RequestMapping(value="/member/memberLogin.do", method=RequestMethod.POST)
	public String memberLogin(@RequestParam String memberId, 
							  @RequestParam String password, 
							  Model model){
	
		//1.업무로직
		//random salt값으로 암호화하는 BCrypt 방식에서의 로그인 체크
		Member m = memberService.selectOneMember(memberId);
		
		 //3.view단 처리
	    String msg = "";
	    String loc = "/";
	    if(m == null){
	        msg = "존재하지 않는 아이디입니다.";			
	    }
	    else{
	        //boolean org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.matches(CharSequence rawPassword, String encodedPassword)
	        if(bcryptPasswordEncoder.matches(password, m.getPassword())){
	            msg = "로그인 성공!";
	            //세션에 로그인한 Member객체 등록
	            model.addAttribute("memberLoggedIn", m);
	        }
	        else{
	            msg = "비밀번호가 틀렸습니다.";
	        }
	    }
	    
	    model.addAttribute("msg", msg);
	    model.addAttribute("loc", loc);
		
		return "common/msg";
	}*/
	
	 @RequestMapping(value="/member/memberLogin.do", method=RequestMethod.POST)
	public ModelAndView memberLogin(ModelAndView mav, @RequestParam String memberId, @RequestParam String password){
		if(logger.isDebugEnabled()) logger.debug("로그인요청!");
		
	 	//리턴할 ModelAndView객체 생성하거나, 파라미터로 설정해 핸들러호출 이전에 생성된 ModelAndView를 사용. 
		ModelAndView mav_ = new ModelAndView();
		
		try{
			
			//1.업무로직
			//random salt값으로 암호화하는 BCrypt 방식에서의 로그인 체크
			Member m = memberService.selectOneMember(memberId);

			//log4j용 예외발생
//			if(true) throw new RuntimeException("내가던진 에러!!!");
			
			//3.view단 처리
			String msg = "";
			String loc = "/";
			if(m == null){
				msg = "존재하지 않는 아이디입니다.";			
			}
			else{
				//boolean org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.matches(CharSequence rawPassword, String encodedPassword)
				if(bcryptPasswordEncoder.matches(password, m.getPassword())){
					msg = "로그인 성공!";
					//세션에 로그인한 Member객체 등록
					mav.addObject("memberLoggedIn", m);
				}
				else{
					msg = "비밀번호가 틀렸습니다.";
				}
			}
			
			//기존 request객체 속성으로 저장했던 것들을 mav에 key/value로 저장함.
			mav.addObject("loc",loc);
			mav.addObject("msg",msg);
			//view단 지정
			mav.setViewName("common/msg");
			
		
		
		} catch (Exception e){
			//logging파일 출력용
			logger.error( "로그인 에러 : ", e);
			//error페이지를 호출하기 위해 다시한번 exception을 던짐
			throw new MemberException("로그인 에러 : "+e.getMessage());
		}
		return mav;
	}
	
	@RequestMapping("/member/memberLogout.do")
	public String memberLogout(SessionStatus sessionStatus, HttpSession session){
		if(logger.isDebugEnabled()) logger.debug("로그아웃요청!");
		
		//현재session상태를 끝났음(Complete)으로 마킹
//		System.out.println("sessionSttus.isComplete()="+sessionStatus.isComplete());
		if(!sessionStatus.isComplete())
			sessionStatus.setComplete();

		return "redirect:/";
	}
	
	@RequestMapping("/member/memberView.do")
	public ModelAndView memberView(@RequestParam String memberId){
		if(logger.isDebugEnabled()) logger.debug("["+memberId+"] 회원정보보기페이지요청");
		ModelAndView mav = new ModelAndView();
		mav.addObject("member",memberService.selectOneMember(memberId));
		mav.setViewName("member/memberView");
		
		return mav;
	}
	
	@RequestMapping("/member/memberUpdate.do")
	public ModelAndView memberUpdate(Member member){
		if(logger.isDebugEnabled()) logger.debug("회원정보수정처리!");
		
		ModelAndView mav = new ModelAndView();
		System.out.println(member);
			
		//1.비지니스로직 실행
		int result = memberService.updateMember(member);
		
		//2.처리결과에 따라 view단 분기처리
		String loc = "/"; 
		String msg = "";
		if(result>0){ 
			msg="회원정보수정성공!";
			mav.addObject("memberLoggedIn", member);
		}
		else msg="회원정보수정실패!";
		
		mav.addObject("msg", msg);
		mav.addObject("loc", loc);
		mav.setViewName("common/msg");
		
		return mav;
	}
	
	/**************  Spring Ajax 시작 **************/
	/**
	 * 1. stream을 이용한 ajax처리
	 * @param memberId
	 * @param mav
	 * @return
	 */
	/*@RequestMapping("/member/checkIdDuplicate.do")
	public void checkIdDuplicate(@RequestParam String memberId, HttpServletResponse response) throws IOException{
		logger.debug("ID중복체크 : 응답객체에 가능여부(boolean) 직접쓰기 ["+memberId+"]");
		
		boolean isUsable = memberService.checkIdDuplicate(memberId)==0?true:false;
		
		response.getWriter().print(isUsable);
		
	}*/
	
	
	/**
	 * 2. jsonView(BeanNameViewResolver)를 이용해서 ModelAndView를 리턴하고, 
	 * 최종적으로 응답객체에 json형식으로 리턴.
	 * 
	 * @param memberId
	 * @param mav
	 * @return
	 */
	/*@RequestMapping("/member/checkIdDuplicate.do")
	public ModelAndView jsonViewcheckIdDuplicate(@RequestParam String memberId, ModelAndView mav) {
		logger.debug("jsonViewCheckIdDuplicate!!");
	    
		Map map = new HashMap();
	    boolean isUsable = memberService.checkIdDuplicate(memberId)==0?true:false;
	    map.put("isUsable", isUsable);
	    logger.debug("isUsable@jsonViewcheckIdDuplicate="+isUsable);
	     
	    mav.addAllObjects(map);
	 
	    //(중요)setViewName에 들어갈 String 파라미터는 JsonView bean 설정해줬던 id와 같아야 한다.
	    mav.setViewName("jsonView");
	    
	    return mav;
	}*/
	
	/**
	 * 3. @ResponseBody어노테이션과 jackson라이브러리를 이용하여 json타입 문자열로 리턴하기
	 * 
	 * @param memberId
	 * @param model
	 * @return
	 * @throws JsonProcessingException 
	 */
	/*@RequestMapping(value="/member/checkIdDuplicate.do")
	@ResponseBody
	public String responseBodycheckIdDuplicate (@RequestParam String memberId) 
			throws JsonProcessingException {
		logger.debug(" String responseBodycheckIdDuplicate!!");
		Map<String, Object> map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		
		boolean isUsable = memberService.checkIdDuplicate(memberId)==0?true:false;
		map.put("isUsable", isUsable);
		jsonStr = mapper.writeValueAsString(map);//JsonProcessingException 예외처리필요
		
		return jsonStr;
	}*/
	
	/**
	 * 4. @ResponseBody어노테이션과 HttpMessageConverter구현객체를 빈으로 등록해서
	 * 자바객체를 json타입으로 리턴하기
	 * 
	 * @param memberId
	 * @param model
	 * @return
	 */
	@RequestMapping("/member/checkIdDuplicate.do")
	@ResponseBody
	public Map<String, Object> responseBodycheckIdDuplicate (@RequestParam String memberId) {
		System.out.println("Map<String, Object> responseBodycheckIdDuplicate!!");
		Map<String, Object> map = new HashMap<>();
		boolean isUsable = memberService.checkIdDuplicate(memberId)==0?true:false;
		
		map.put("isUsable", isUsable);
		return map;
	}
	
	
	
	/**************  Spring Ajax 끝 **************/
}
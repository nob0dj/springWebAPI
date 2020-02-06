package com.kh.spring.summernote.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.aws.model.service.AWSService;
import com.kh.spring.aws.model.vo.S3Object;
import com.kh.spring.summernote.model.service.SummernoteService;
import com.kh.spring.summernote.model.vo.Summernote;

@Controller
public class SummernoteController {
	
	static final Logger logger = LoggerFactory.getLogger(SummernoteController.class);
	
	@Autowired
	SummernoteService summernoteService;
	
	@Autowired
	AWSService awsService;
	
	@ModelAttribute
	public void common(Model model) {
		model.addAttribute("pageTitle", "SummerNote Api");
	}
	
	@GetMapping("/")
	public String index(Model model) {
		logger.debug("{}", "[/] : index페이지 요청!");
		return "index";
	}
	
	@GetMapping("/summernote/form")
	public String summernote(Model model) {
		logger.debug("{}", "[/summernote] : summernote 폼페이지 요청!");
		model.addAttribute("pageSubTitle", "글쓰기");
		
		return "summernote/form";
	}

	@GetMapping("/summernote/list")
	public String summernoteList(Model model) {
		logger.debug("{}", "[/summernote] : summernote 목록 요청!");
		model.addAttribute("pageSubTitle", "목록");
		
		List<Summernote> list = summernoteService.findAll();
		
		/*목록에 뿌려주기 위한 contents작업 : html->text, 글자수제한*/
		list = list
				.stream()
			    .filter(s -> !"temp".equals(s.getWriter())) //임시파일제외
				.peek(summernote -> {
					String contents = summernote.getContents()
												.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");//html태그 제외하고 목록에 보여주기
					if(contents.length() > 10) 
						contents = contents.substring(0, 10);
					
					summernote.setContents(contents);
					
				})
				.collect(Collectors.toList());//TerminalOperation 
		
		model.addAttribute("list", list);
		
		return "summernote/list";
	}
	
	
	/**
	 * 글쓰기 등록
	 * 
	 * @param model
	 * @param writer
	 * @param contents
	 * @param file
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/summernote/insert")
	public String summernoteInsert(Model model, 
								   @ModelAttribute Summernote summernote,
								   @RequestParam(value="file", required=false) MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		logger.debug("{}", "[/insertSummernote.do] : 게시글 등록 요청");
		logger.debug("summernote={}", summernote);
		logger.debug("file={}", file);//null
		
		//현재시각 대입
		summernote.setRegDate(new Date());
		
		logger.debug("note={}",summernote);
		
		summernote = summernoteService.save(summernote);
		
		//등록여부에 따른 분기처리
		redirectAttributes.addFlashAttribute("msg", summernote.isNew()?"등록실패!":"등록성공!");
		
		return "redirect:/summernote/view/"+summernote.getId();
	}
	
	
	/**
	 * summernote에서 image를 선택할 경우, ajax로 이미지만 파일업로드 처리하고, 저장된 파일명을 리턴한다.
	 * 
	 * @param file
	 * @param request
	 * @return
	 */
	@PostMapping("/summernote/image")
    @ResponseBody
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file,
    										  HttpServletRequest request) {
        try {
//            UploadFile uploadedFile = imageService.store(file);
        	
        	logger.debug("file={}", file);

        	//파일 저장경로
        	String saveDirectory = request.getSession().getServletContext().getRealPath("/upload");
        	File dir = new File(saveDirectory);
        	logger.debug("dir={}", dir);
    		if(dir.exists()==false) 
    			dir.mkdirs();
    		
    		
    		//파일명 재생성
			String originalFileName = file.getOriginalFilename();
			String ext = originalFileName.substring(originalFileName.lastIndexOf(".")+1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
			int rndNum = (int)(Math.random()*1000);
			String renamedFileName = sdf.format(new Date(System.currentTimeMillis()))+"_"+rndNum+"."+ext;
			try {
				//해당경로에 파일 저장
				//java.io.File.File(String parent, String child)
				file.transferTo(new File(saveDirectory,renamedFileName));
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
            return ResponseEntity.ok().body("upload/" + renamedFileName);//이미지 저장된 경로+파일명을 리턴함. @ResponseBody
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

	
	@PostMapping("/summernote/s3/image")
    @ResponseBody
    public ResponseEntity<?> handleFileUploadViaS3(@RequestParam("file") MultipartFile file,
    											   @RequestParam("id") Long id,
    											   HttpServletRequest request) {
		logger.debug("{}", "[/summernote/s3/image] : 이미지 s3 업로드 요청!");
		String saveDirectory = request.getSession().getServletContext().getRealPath("/upload");
		
		logger.debug("id={}", id);
		
		//첨부파일 저장을 위해 우선 게시글 번호 생성
		if(id == 0) {
			Summernote summernote = new Summernote();
			summernote.setWriter("temp");//필수 항목
			summernote = summernoteService.save(summernote);
			logger.debug("id={}", summernote.getId());		
			id = summernote.getId();
		}
		
		
        try {
        
        	S3Object s3obj = awsService.store(saveDirectory, file);
        	logger.debug("s3obj={}", s3obj);
        	
        	Map<String, Object> map = new HashMap<>();
        	map.put("id",id);
        	map.put("insertImage",s3obj.getResourceUrl());
        	return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        	
//            return ResponseEntity.ok().body(s3obj.getResourceUrl());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
	
	/**
	 * 상세보기 페이지 
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/summernote/view/{id}")
	public String summernoteView(Model model, @PathVariable("id") Long id) {
		logger.debug("{}", "[/summernote/"+id+"] : summernote 상세보기 페이지 요청!");
		
		model.addAttribute("pageSubTitle", "상세보기|수정");
		Optional<Summernote> maybeSummernote = summernoteService.findById(id);
		model.addAttribute("summernote", maybeSummernote.get());
		
		return "summernote/view";
	}
	
	/**
	 * 수정
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@PostMapping("/summernote/update")
	public String summernoteUpdate(Model model, @ModelAttribute Summernote summernote, RedirectAttributes redirectAttributes) {
		logger.debug("{}", "[/summernote/update] : summernote 수정 요청!");
		logger.debug("sumernote={}", summernote);
		logger.debug("sumernote.id={}", summernote.getId());
		
		summernoteService.save(summernote);
		
		//등록여부에 따른 분기처리
		redirectAttributes.addFlashAttribute("msg", "수정성공!");
		
		return "redirect:/summernote/view/"+summernote.getId();
	}
	
	/**
	 * 삭제
	 * 
	 * @param model
	 * @param id
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/summernote/delete/{id}")
	public String summernoteDelete(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		logger.debug("{}", "[/summernote/"+id+"] : summernote 삭제  요청!");
		
		summernoteService.deleteById(id);
		redirectAttributes.addFlashAttribute("msg", "삭제성공!" );
		
		return "redirect:/summernote/list";
	}
	
}

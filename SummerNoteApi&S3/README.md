# Summer Note API

[[Summernote] Summernote (썸머노트) 사용법](https://ninearies.tistory.com/123)  

## DB설정
jpa가 지원하는 ddl-auto 기능을 사용할 수도 있지만, 직접 db설정함.

@sqldeveloper
spring계정으로 진행. 
sequence는 @Id컬럼에 따로 지정안하는 경우, jpa의 hibernate_sequence객체를 이용하게 된다.

  --==============================================================
  -- summernote테이블 생성
  --==============================================================
  create table summernote(
    id number,
    writer varchar2(256),                   --글쓴이: not null처리 안함
    contents clob,                          --내용: not null처리 안함
    reg_date date default sysdate,
    is_temp char(1) default 'N',            --임시파일여부
    constraint pk_summernote primary key(id),
    constraint ck_summernote check(is_temp in('Y','N'))
  );

  create sequence seq_summernote;

  select * from summernote order by id desc;


## 프로젝트 설정
sample프로젝트에서 시작할 수 있음.
* lombok
* web
* devtools
* jpa


@pom.xml
thymeleaf관련 layout사용을 위해 dependency추가

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
      </dependency>
      <!-- layout을 사용하기 위한 의존  -->
      <dependency>
          <groupId>nz.net.ultraq.thymeleaf</groupId>
          <artifactId>thymeleaf-layout-dialect</artifactId>
      </dependency>

@application.yml

    #application.yml
    server:
      port: 9999
      servlet:
        context-path: /summernote-s3
      
    #logging
    logging:
      level:
        com.kh.spring: DEBUG
        
    spring:
      datasource:
        driver-class-name: oracle.jdbc.driver.OracleDriver
        url: jdbc:oracle:thin:@localhost:1521:xe
        username: spring
        password: spring
      jpa:
        show-sql: true
    #    generate-ddl: true #시작시 스키마 초기화 여부
        database: oracle #multiple db를 사용할 경우, 각각에 알맞은 dialect사용
    #    hibernate:
    #      ddl-auto: create  
      


## summernote 환경설가
2번cdn방식으로 처리함.

1. summernote 소스를 localhost서버에서 배포
   * [https://summernote.org/](https://summernote.org/)에서 다운로드후 압축해제.
   * dist폴더이하를 프로젝트에 복사한 후 summernote로 이름 변경


2. CDN으로 처리하기

@src/main/resources/template/fragments/head.html
bootstrap4버젼으로 처리 하기 위해서 summernote-bs4를 이용함.

    <!-- SummerNote관련 라이브러리 Start -->
    <script src="https://code.jquery.com/jquery-3.4.1.min.js" crossorigin="anonymous"></script>
      <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>

      <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
      <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>

      <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.15/dist/summernote-bs4.min.css" rel="stylesheet">
      <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.15/dist/summernote-bs4.min.js"></script>
    <!-- SummerNote관련 라이브러리 End-->


@src/main/resources/templates/index.html
summernote함수 호출코드에서 height속성이 없다면, 작성된 내용에 따라 높이가 정해지며, 
높이를 지정하게 되면, 해당 높이 안에서 scrollbar가 생성된다.

    <div layout:fragment="content">
      <img th:src="@{/images/logo-spring.png}" id="center-image" alt="" />
    </div>

@com.kh.spring.summernote.controller.SummernoteController

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


@src/main/resources/templates/summernote/form.html

    
    <style>
    .summernote-container {
      width: 80%;
      margin: 0 auto;
      padding: 10px;
    }
    #writer, #btn-register {
      margin-top: 10px;
      margin-bottom: 10px;
    }
    </style>

    <div layout:fragment="content">
      
      <div class="summernote-container">
        <form th:action="@{/summernote/insert}" method="post" enctype="multipart/form-data">
          <!-- 글쓴이 input태그 -->
          <div class="input-group flex-nowrap">
            <div class="input-group-prepend">
              <span class="input-group-text" id="writer">글쓴이</span>
            </div>
            <input type="text" class="form-control" id="writer" name="writer" placeholder="글쓴이(필수)" required/>
          </div>
          
          <!-- summernote api 대상 태그 : div 또는 textarea-->
          <!-- <div id="summernote">Hello Summernote</div> -->
          <textarea id="summernote" name="contents"></textarea>
          
          <!-- submit 버튼 : button태그의 기본 type속성은 submit이다.-->
          <button class="btn btn-outline-success btn-block" id="btn-register">등록</button>
        </form>
      </div>
      
    </div>


summernote api 적용 

    <script th:inline="javascript">
      $(()=> {
        $("#summernote").summernote({
          height: 300,                 // set editor height
          minHeight: null,             // set minimum height of editor
          maxHeight: null,             // set maximum height of editor
          focus: true                  // set focus to editable area after initializing summernote
        });
        
      });

     
      </script>

@com.kh.spring.summernote.controller.SummernoteController
별도의 사용자 msg전달 페이지 없이 RedirectAttributes를 이용한다.


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
   								   @RequestParam("writer") String writer,
   								   @RequestParam("contents") String contents,
   								   @RequestParam(value="file", required=false) MultipartFile file,
   								   RedirectAttributes redirectAttributes) {
   		logger.debug("{}", "[/insertSummernote.do] : 게시글 등록 요청");
   		logger.debug("writer={}", writer);
   		logger.debug("contents={}", contents);
   		logger.debug("file={}", file);//null
   		
   		
   		Summernote summernote = new Summernote();
   		summernote.setWriter(writer);
   		summernote.setContents(contents);
   		summernote.setRegDate(new Date());
   		
   		logger.debug("note={}",summernote);
   		
   		summernote = summernoteService.save(summernote);
   		
   		//등록여부에 따른 분기처리
   		redirectAttributes.addFlashAttribute("msg", summernote.isNew()?"등록실패!":"등록성공!");
   		
   		return "redirect:/summernote/view/"+summernote.getId();
   	}

@com.kh.spring.summernote.model.vo.Summernote
* `AbstractPersistable<Long>`을 상속하므로 id필드는 생략가능하지만, `사용자입력값 - command객체` 필드 바인딩시에 id값이 누락되므로
명시적으로 표기함.
* regDate필드와 같이 `java.util.Date`타입이지만, 날짜/시간 데이터를 모두 입력하기 위해 `@Temporal(TemporalType.TIMESTAMP)`어노테이션을 지정한다.(기본값이므로 생략가능)
  * `@Temporal(TemporalType.DATE)`: 날짜만 db에 저장됨.
  * `@Temporal(TemporalType.TIME)`: 시각정보만 db에 저장됨.
  * pattern을 이용해서 처리할 수 있다. (MM:월, mm:분, hh:시각(12), HH:시각(24))
  * [https://docs.spring.io/spring/docs/3.0.x/javadoc-api/org/springframework/format/annotation/DateTimeFormat.ISO.html](https://docs.spring.io/spring/docs/3.0.x/javadoc-api/org/springframework/format/annotation/DateTimeFormat.ISO.html)
* 임시저장글 여부를 나타내는 isTemp필드는 Boolean - db colomun char(1) 타입간의 변환을 위해 `@Type(type="yes_no")`을 사용한다.
  * `@Type(type="yes_no")` => 'Y', 'N'로 값이 변환되어 저장된다.
  * `@Type(type="true_false")` => 'T', 'F'로 값이 변환되어 저장된다.
  * [Configure hibernate (using JPA) to store Y/N for type Boolean instead of 0/1](https://stackoverflow.com/questions/1154833/configure-hibernate-using-jpa-to-store-y-n-for-type-boolean-instead-of-0-1)
  * `@Converter`어노테이션을 이용해 타입변환을 명시할 수 있다. mybatis의 typeHandler와 유사하다.
    * `com.kh.spring.summernote.model.converter.BooleanToStringConverter` @Converter 클래스 참조
  * 기본값 지정을 위해 필드의 값을 명시하였다.
* id, tempFlag등은 db의 null값도 처리하기 위해 기본형이 아닌 wrapper class를 사용하도록 한다.


        @Entity
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        public class Summernote extends   AbstractPersistable<Long> implements Serializable {


          private static final long serialVersionUID = 1L;

          /**
          * AbstractPersistable을 상속했으므로 id필드를 생략해도 되지만, 
          * command객체 필드 바인딩을 위해서 명시함.
          * 생략한 경우, 사용자입력값 id가 커맨드객체 id에 대입되지 않는다.
          * 
          */
          @Id
          Long id;

          @NonNull
          String writer;

          @NonNull
          String contents;

          /*input:date => java.util.Date타입으로  자동변환을 위해 포맷지정*/
          //	@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
          //	@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)
          @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")//input:datetime-local에서 작성한 사용자 입력값 처리를 위한 포맷 지정
          
          /* 날짜/시각 정보를 모두 지정하기 위해 아래 @Temporal어노테이션을 사용한다.(기본값이므로 생략가능. DATE, TIME을 사용하면 해당정보만 저장됨)*/
          @Temporal(TemporalType.TIMESTAMP)
          Date regDate = new Date();

          /**
          * db column is_temp char(1) 'Y','N' default 'N'
          * entity attr isTemp:Boolean
          */
          //	@Convert(converter=BooleanToStringConverter.class)
          @Type(type="yes_no")
          @Column(name="is_temp")
          private Boolean tempFlag = false;
        }




@com.kh.spring.summernote.model.service.SummernoteService

  public interface SummernoteService {

    Summernote save(Summernote note);

  }

@com.kh.spring.summernote.model.service.SummernoteServiceImpl

  @Service
  public class SummernoteServiceImpl implements SummernoteService {

    @Autowired
    SummernoteRepository summernoteRepository;

    @Override
    public Summernote save(Summernote note) {
      return summernoteRepository.save(note);
    }

  }

@com.kh.spring.summernote.model.repository.SummernoteRepository
`@Repository` 어노테이션 생략가능

  public interface SummernoteRepository extends JpaRepository<Summernote, Long> {

  }


@Test


## view 상세보기 페이지

@com.kh.spring.summernote.controller.SummernoteController


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

@com.kh.spring.summernote.model.service.SummernoteService

    Optional<Summernote> findById(Long id);

@com.kh.spring.summernote.model.service.SummernoteServiceImpl
CRUDRepository인터페이스의 추상메소드를 호출함. 이후 Repository 단에서 작성코드 없음.


  @Override
	public Optional<Summernote> findById(Long id) {
		return summernoteRepository.findById(id);
	}



@src/main/resources/templates/summernote/view.html

form.html에서 복붙함.

등록후 상세보기페이지 올경우, RedirectAttributes에 저장된 msg를 사용자에게 전달할 수 있어야 한다.

view model의 속성 summernote를 화면 출력함.
`th:object="${summernote}"`가 기술된 form태그의 자식에서 타임리프의 메세지출력식`*{}`을 이용해서 계층구조의 필드값 출력
`#summernote`태그의 자식태그로 contents필드값을 출력하면, 에디터 영역에 사용자 입력값이 그대로 보이게 된다.

작성날짜는 readonly속성을 이용하여 출력만 하도록한다.
* `<input type="datetime-local" class="form-control" name="regDate" id="regDate" th:value="*{#dates.format(regDate, 'yyyy-MM-dd''T''HH:mm:ss')}" readonly/>`
* type은 datetime(deprecated)이 아닌 datetime-local을 사용
* value는 ISO 8601방식으로 출력.
* thymeleaf의 date형식을 사용하된 T를 표현해야 한다. T는 포맷문자가 아니므로 'T'문자열처리+escaping처리한다.

        <script th:inline="javascript">
        $(()=> {
          $("#summernote").summernote({
            height: 300,                 // set editor height
            minHeight: null,             // set minimum height of editor
            maxHeight: null,             // set maximum height of editor
            focus: true                  // set focus to editable area after initializing summernote
          });
          
        });
        </script>

        <style>
        .summernote-container {
          width: 80%;
          margin: 0 auto;
          padding: 10px;
        }
        #writer, #regDate, #btn-update {
          margin-top: 10px;
          margin-bottom: 10px;
        }
        </style>

        <div layout:fragment="content">
          <div class="summernote-container">
            <form th:action="@{/summernote/update}" th:object="${summernote}" method="post" enctype="multipart/form-data">
              <input type="hidden" name="id" th:value="*{id}" />
              
              <!-- 글쓴이 input태그 -->
              <div class="input-group flex-nowrap">
                <div class="input-group-prepend">
                  <span class="input-group-text" id="writer">글쓴이</span>
                </div>
                <input type="text" class="form-control" id="writer" name="writer" th:value="*{writer}" placeholder="글쓴이(필수)" required/>
              </div>
              
              <!-- summernote api 대상 태그 : div 또는 textarea-->
              <!-- <div id="summernote">Hello Summernote</div> -->
              <textarea id="summernote" name="contents" th:text="*{contents}"></textarea>
              
            <input type="datetime-local" class="form-control" name="regDate" id="regDate" th:value="*{#dates.format(regDate, 'yyyy-MM-dd''T''HH:mm:ss')}" readonly/>
              
              <!-- submit 버튼 : button태그의 기본 type속성은 submit이다.-->
              <button class="btn btn-outline-info btn-block" id="btn-update">수정</button>
            </form>
            <!-- <form th:action="@{'/summernote/delete/'+${id}}" method="post" > -->
            <form th:action="@{/summernote/delete/{id}(id=${summernote.id})}" method="post" >
              <button class="btn btn-outline-danger btn-block" id="btn-delete">삭제</button>
            </form>
          </div>
        </div>

@src/main/resources/tempaltes/fragments/head.html

    <!-- RedirectAttributes를 이용한 사용자메세지 처리용 -->
    <script th:if="${msg}">
      alert("[[${msg}]]");
    </script>


## list 목록 페이지

@com.kh.spring.summernote.controller.SummernoteController

    @GetMapping("/summernote/list")
    public String summernoteList(Model model) {
      logger.debug("{}", "[/summernote] : summernote 목록 요청!");
      model.addAttribute("pageSubTitle", "목록");
      
      List<Summernote> list = summernoteService.findAll();
      
      /*목록에 뿌려주기 위한 contents작업 : html->text, 글자수제한*/
      list = list
          .stream()
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

@com.kh.spring.summernote.model.service.SummernoteService

  List<Summernote> findAll();


@com.kh.spring.summernote.model.service.SummernoteServiceImpl
정렬처리를 위해 Sort객체를 이용함.
메소드명을 `findAllByOrderByIdDesc()`와 같이 설정해도 가능하다.

  @Override
	public List<Summernote> findAll() {
		return summernoteRepository.findAll(new Sort(Sort.Direction.DESC, "id"));
	}


@src/main/resources/templates/summernote/list.html

    <style>
    .summernote-container {
      width: 80%;
      margin: 0 auto;
      padding: 10px;
    }
    tbody tr {
      cursor: pointer;
    }
    </style>

    <script th:inline="javascript">
    function goPage(id){
      let ctx = /*[[@{/}]]*/;
      location.href = ctx + "summernote/view/"+id;
    }
    </script>

    <div layout:fragment="content">
      <div class="summernote-container">
       
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col">No.</th>
              <th scope="col">글쓴이</th>
              <th scope="col" class="contents">내용</th>
              <th scope="col">작성일</th>
            </tr>
          </thead>
          <tbody>
          <tr scope="row" th:each="s:${list}" th:onclick="goPage([[${s.id}]]);">
            <td th:text="${s.id}"></td>
            <td th:text="${s.writer}"></td>
            <!-- unescapedtext를 사용 -->
            <td th:text="${s.contents}"></td>
            <td th:text="${s.regDate}"></td>
          </tr>
          </tbody>
        </table>
      
      </div>
    </div>



## 이미지서버저장 ajax 처리
기본적으로 summernote에서 이미지를 첨부하게 되면, **inline data uri scheme**을 이용하여, 태그내에 이미지를 저장한다.
이미지파일의 이진데이터를 Base64방식으로 인코딩한 값을 data uri scheme에 기록하여 사용. 이를 **immediate data**라고 한다. 
마치 외부에서 이미지를 참조하는 것과 같이 사용할 수 있다.

[Inline Images with Data URLs](http://www.websiteoptimization.com/speed/tweak/inline-images/)

[Summernote 이미지 업로드 예제](https://devofhwb.tistory.com/90)



@src/main/resources/templates/summernote/form.html
summernote api에 이미지 업로드시 콜백함수가추가

    <script>
      $(()=> {
        $("#summernote").summernote({
          ...
          callbacks: {
                onImageUpload: function(files, editor, welEditable) {
                
                  for (var i = files.length - 1; i >= 0; i--) {
                    sendFile(files[i], this); // this => #summernote
                  }
                }
              }
        });
        
      });
      function sendFile(file, el) {
        console.log("el=",el);//<textarea id="summernote" name="summernote" style="display: none;"></textarea>
        
          var form_data = new FormData();
          form_data.append('file', file);//file -> handler에서 접근할 전송파일명 
          
          let ctx = /*[[@{/}]]*/; // script태그 안에서 ctx-path 표현하기 : "\/spring\/"으로 치환됨

          $.ajax({
            data: form_data,
            type: "POST",
            url: ctx+'summernote/image',
            cache: false,
            contentType: false,
            enctype: 'multipart/form-data',
            processData: false,
            success: function(data) {
            console.log("data =",data);
              $(el).summernote('editor.insertImage', ctx+data);
            }
          });
        }
    
    </script>

@com.kh.spring.summernote.controller.SummernoteController


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
        
        logger.debug("file={}", file);

        //파일 저장경로
        String saveDirectory = request.getSession().getServletContext().getRealPath("/upload");
        File dir = new File(saveDirectory);
        logger.debug("dir={}", dir);
        //디렉토리 생성
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


## update 게시글 수정

## delete 게시글 삭제
단순 게시글 삭제.
이후 s3이미지업로드 구현후에는 게시글에 사용된 이미지도 모두 삭제처리함.




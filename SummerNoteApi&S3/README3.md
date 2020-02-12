# 파일 관리
* 목록조회
* 선택파일 일괄 삭제

s3에 업로드한 파일의 메타정보는 db s3object테이블에서 별도로 관리하며, 페이지 상세보기에서 파일만 따로 다운로드 받을 수 있도록한다.

@sqldeveloper
시퀀스는 entity클래스에서 별도의 설정이 없다면, jpa가 제공하는 hibernate_sequence객체에서 자동 채번된다.


  --==============================================================
  -- s3object 테이블 생성
  --==============================================================
  --s3에 업로드된 파일은 객체(object)라고 부른다.

  create table s3object(
      id number,
      original_filename varchar2(256) not null,
      renamed_filename varchar2(256) not null,
      resource_url varchar2(512) not null, 
      content_type varchar2(256), 
      file_size number, 
      download_count number default 0,
      reg_date date default sysdate,
      constraint pk_s3object primary key(id)
  );
  --drop table s3object;
  --truncate table s3object;
  create sequence seq_s3object;

  select 
      * 
  from 
      s3object
  order by id desc;


@com.kh.spring.aws.model.vo.S3Object
* aws라이브러리에서 제공하는 S3Object도 있으니, import시에 주의할 것.
* 필드명과 컬럼명이 다르다면, `@Column(name)`속성을 반드시 작성한다.
  * 기본적으로 db컬럼명을 찾을 때, originalfilename 또는 camelCasing을 기준으로 original_file_name컬럼을 찾는다.
  

      @Entity
      @Data
      public class S3Object extends AbstractPersistable<Long> implements Serializable{
          
          /**
        * 
        */
        private static final long serialVersionUID = 1L;

        /**
        * 기본적으로 db컬럼명을 찾을 때, originalfilename 또는 camelCasing을 기준으로 original_file_name컬럼을 찾는다.
        */
        @Column(name="original_filename")
          String originalFileName;
          
          @Column(name="renamed_filename")
          String renamedFileName;
          
          @Column
          String resourceUrl;
          
          @Column
          String contentType;
          
          /**
          * oracle에서 size는 예약어라 컬럼명으로 사용할 수 없다.
          */
          @Column(name="file_size")
          long size;
          
          @Column
          long downloadCount;
          
          @Column
          Date regDate;
      }

## 게시글 등록시 image첨부가 있는 경우
이미지 첨부시 먼저 게시글 번호를 부여 받아서, db에 해당게시글의 첨부파일로 등록한다.

@templates/summernote/form.html
id필드값을 함께 전송한다. 
최초 이미지 첨부시, id=0이 전송되고, 이미지업로드 ajax success함수에서 게시글 id를 함께 리턴받아 input#id에 값대입함.


    <form th:action="@{/summernote/insert}" method="post" enctype="multipart/form-data">
        <input type="hidden" name="id" id="id" value="0"/> <!-- 초기값 0으로 설정 -->
        
        ...

    </form>   
    <script>
    
    function sendFile(file, el) {
        var formData = new FormData();
        formData.append('file', file);//file -> handler에서 접근할 전송파일명 
        formData.append('id', $("#id").val());
        
        let ctx = /*[[@{/}]]*/; // script태그 안에서 ctx-path 표현하기 : "\/spring\/"으로 치환됨

        $.ajax({
          url: ctx+'summernote/s3/image',
          ...
          success: function(data) {
           
            //aws s3에 업로드시
            $(el).summernote('editor.insertImage', data.insertImage);
            $("#id").val(data.id);
          }
        });
      }
    </script>     

  
@com.kh.spring.summernote.controller.SummernoteController
form전송된 id처리
awsService의 store메소드 요청시 id값 함께 전송

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
        summernote.setTempFlag(true);
        summernote = summernoteService.save(summernote);
        logger.debug("summernote={}", summernote);		
        id = summernote.getId();
      }
      
		
      try {
      
        //s3에 파일 업로드 및 db저장
        S3Object s3obj = awsService.store(saveDirectory, id, file);
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

@com.kh.spring.aws.model.service.AWSService

    @Override
    public S3Object store(String saveDirectory, Long id, MultipartFile file) {
      ....

      //임시파일 삭제
      tempFile.delete();
      
      //db에 s3object정보 저장
      s3obj = save(s3obj);
      
      return s3obj;
    }

    private S3Object save(S3Object s3obj) {
      s3obj = awsRepository.save(s3obj);
      return s3obj;
    }



@com.kh.spring.aws.model.repository.AWSRepository
JpaRepository에서 기본 제공되는 추상메소드는 작성할 필요없음. 

  public interface AWSRepository extends JpaRepository<S3Object, Long> {

  }



## summernote | s3object table 1:N관계 설정하기

@com.kh.spring.summernote.model.vo.Summernote

    /**
    * mappedBy속성을 통해 읽기 전용으로 처리함.
    * mappedBy속성이 없으면, summernote_file_list 테이블을 찾는다
    */
    @OneToMany(mappedBy="summernote")
    private List<S3Object> fileList;


@com.kh.spring.aws.model.vo.S3Object

    @ManyToOne
    Summernote summernote;


## 파일다운로드

@templates/summernote/view.html
객체의 존재여부에 따라 태그를 표시하고자 할때, `th:if=${instatnce.empty}`처리한다.
[https://stackoverflow.com/questions/33106391/how-to-check-if-list-is-empty-using-thymeleaf](https://stackoverflow.com/questions/33106391/how-to-check-if-list-is-empty-using-thymeleaf)


    <div class="summernote-container">
      ...
      
      <hr th:if="${!summernote.fileList.empty}"/>
		  
      <h4 th:if="${!summernote.fileList.empty}">첨부파일</h4>
      <ul th:each="f:${summernote.fileList}" id="fileList-container">
        <li><a href="#" onclick="fileDownload(this);" th:data-rname="${f.renamedFileName}" th:data-oname="${f.originalFileName}" th:text="${f.originalFileName}"></a></li>
      </ul>
    </div>

    <style>
    ul#fileList-container {
      list-style: none;
      padding-left: 20px;
    }
    </style>

    <script>
    /**
    * thymeleaf에 사용된 사용자 속성명은 모두 소문자로 처리된다.
    * s3에서 resourceurl로 파일을 다운로드하는 api는 없다.
    * bucketname, 저장된  폴더명, key값(저장된 파일명)이 필요하다.
    */
    function fileDownload(a){
      const oname = $(a).attr("data-oname");
      const rname = $(a).attr("data-rname");
      console.log(oname, rname);
      
      location.href = ctx 
              + "summernote/s3/images/download"
              + "?oname="+oname
              + "&summernoteid=[[${summernote.id}]]"
              + "&rname="+rname;
    }
    
    
    </script>


@com.kh.spring.summernote.controller.SummernoteController

    @GetMapping("/summernote/s3/images/download")
    @ResponseBody
    public ResponseEntity<Resource> handleFileDownload(@RequestParam("oname") String originalFileName,
                              @RequestParam("summernoteid") String summernoteId, 
                              @RequestParam("rname") String renamedFileName,
                              @RequestHeader("User-Agent")String userAgent){
      
      logger.debug("{}, {}, {}", "[/summernote/s3/images/download] : 파일다운로드 요청!", originalFileName, renamedFileName);
      
      Resource resource = awsService.loadFileAsResource(summernoteId, renamedFileName);
      
      HttpHeaders headers = new HttpHeaders();
      try {
        String resFileName = null;
        //user-Agent의 정보를 파라미터로 받아 브라우저별 처리
        //IE 브라우저 엔진 이름 조회
        if(userAgent.contains("Trident")) {	
          resFileName = URLEncoder.encode(originalFileName, "UTF-8").replaceAll("\\+","%20");
        }
        else if(userAgent.contains("Edge")) {
          resFileName = URLEncoder.encode(originalFileName, "UTF-8");
        }
        // 크롬 등 표준 브라우져
        else{	
          resFileName = new String(originalFileName.getBytes("UTF-8"), "ISO-8859-1");
        }
        logger.debug("resFileName={}",resFileName);
        
        headers.add("Content-Disposition", "attachment; filename="+resFileName);
        
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      
      return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }


@com.kh.spring.aws.model.service.AWSServiceImpl
amazonS3, bucketName은 필드로 선언해서 메소드간 공유함.

    @Override
    public Resource loadFileAsResource(String summernoteId, String renamedFileName) {

      logger.debug("{}, {}",bucketName, summernoteId+"/"+renamedFileName);
      GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, "images/"+summernoteId+"/"+renamedFileName);
      com.amazonaws.services.s3.model.S3Object s3object = amazonS3.getObject(getObjectRequest);
      S3ObjectInputStream s3is = s3object.getObjectContent();
      
      byte[] bytes = null;
      try {
        bytes = IOUtils.toByteArray(s3is);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      return new ByteArrayResource(bytes);
    }



## S3파일목록 가져오기
@/templates/fragments/header.html

    <li class="nav-item"><a class="nav-link" th:href="@{/summernote/s3/list}">S3파일목록보기</a></li>

@com.kh.spring.summernote.controller.SummernoteController

    @GetMapping("/summernote/s3/list")
    public String summernoteS3ObjectList(Model model) {
      logger.debug("{}", "[/summernote/s3/list] : s3파일 목록 요청!");
      model.addAttribute("pageSubTitle", "S3 파일 목록");
      
      List<S3ObjectSummary> list = awsService.findAll();
      
      
      model.addAttribute("list", list);
      
      return "summernote/s3List";
    } 


@com.kh.spring.aws.model.service.AWSServiceImpl
* listObjectRequest
* objectListing : 반복해서 접근하면서, marker를 지정하고, 모든 파일을 가져올 수 있도록 한다.
* S3ObjectSummary
  * key
  * owner
  * size
  * lastModifed



    @Override
    public List<S3ObjectSummary> findAll() {
      List<S3ObjectSummary> list = new ArrayList<>();
        
      ListObjectsRequest listObjectRequest = new ListObjectsRequest();
      listObjectRequest.setBucketName(bucketName);
      listObjectRequest.setPrefix("images");
      
      ObjectListing objectListing = null;
      do {
        objectListing = amazonS3.listObjects(listObjectRequest);
        
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
          logger.debug("key = {}", objectSummary.getKey());
          logger.debug("owner = {}", objectSummary.getOwner());
          logger.debug("size = {}", objectSummary.getSize());
          logger.debug("lastModified = {}", objectSummary.getLastModified());
          list.add(objectSummary);
        }
        //파일을 1000개단위로 가져오며,모든 파일을 가져올수 있다.
        objectListing.setMarker(objectListing.getNextMarker());

      } while(objectListing.isTruncated());//objectListing의 완료여부를 리턴함. not complete일때 true.
      
      return list;
    }


@/templates/fragments/summernote/s3List.html

    
    <style>
    .summernote-container {
      width: 80%;
      margin: 0 auto;
      padding: 10px;
    }
    tbody tr .key {
      cursor: pointer;
    }
    </style>

    <script th:inline="javascript">
    function openFile(key){
      open("https://shqkel1863-summernote.s3.ap-northeast-2.amazonaws.com/"+key);	
    }
    </script>

    <div layout:fragment="content">
      <div class="summernote-container">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col">No.</th>
              <th scope="col">파일명(key)</th>
              <th scope="col">파일크기(byte)</th>
              <th scope="col">등록일</th>
            </tr>
          </thead>
          <tbody>
          <tr scope="row" th:each="s3,status:${list}" >
            <td th:text="${status.index+1}"></td>
            <td class="key" th:onclick="openFile([[${s3.key}]]);" th:text="${s3.key}"></td>
            <td th:text="${s3.size}"></td>
            <td th:text="${#dates.format(s3.lastModified, 'yyyy-MM-dd HH:mm')}"></td>
          </tr>
          </tbody>
        </table>
        <button type="submit" class="btn btn-outline-danger btn-lg btn-block">선택파일삭제</button>
      </div>
    </div>


## s3 파일 일괄 삭제


@/templates/fragments/summernote/s3List.html

    <form th:action="@{/summernote/s3/delete}" id="s3keyDelFrm" method="POST">
      <table class="table table-hover">
        <thead>
          <tr>
            ...
            <th scope="col"><input type="checkbox" id="s3ListAll" /></th>
            ...
          </tr>
        </thead>
        <tbody>
          <tr scope="row" th:each="s3,status:${list}" >
            ...
            <td><input type="checkbox" name="s3key" th:value="${s3.key}"/></td>
            ...
          </tr>
        </tbody>
      </table>
    </form>

  

@com.kh.spring.summernote.controller.SummernoteController

    @PostMapping("/summernote/s3/delete")
    public String summernoteS3ObjectDelete(Model model,
                        RedirectAttributes redirectAttributes,
                        @RequestParam("s3key") String[] s3keys) {
      logger.debug("{}", "[/summernote/s3/delete] : s3파일 삭제 요청!");
      
      awsService.deleteObject(s3keys);
      
      redirectAttributes.addFlashAttribute("msg", "삭제성공!" );
      
      return "redirect:/summernote/s3/list";
    } 

@com.kh.spring.aws.model.service.AWSServiceImpl

    @Override
    public void deleteObject(String[] s3keys) {
      Arrays.stream(s3keys)
          .forEach(key -> {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
            amazonS3.deleteObject(deleteObjectRequest);
          });
    }
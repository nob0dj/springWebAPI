# S3
aws에서 제공하는 파일서버 Simple Storage Service의 앞글자를 따서 S3라고 부른다.
업로드될 파일(객체라고 부름)은 기본적으로 모두 private으로 외부에서 url로 접근할 수 없으므로, puclic으로 설정해야 한다.
bucket이하의 폴더구조를 가질 수 있지만, 이는 실제 디렉토리가 아니라 파일(객체)의 key값의 접두어로 사용된다.

* bucket : 저장소단위
* object: 저장될 파일


## 사용자계정 생성 IAM 
s3를 사용할 계정을 생성한다. IAM에는 과금되지 않고, s3에 업로드한 파일에 따라 과금된다.
[aws-sdk-java에서 aws s3 사용하는 법](https://galid1.tistory.com/590)

**IAM에서 사용자 생성하고 발급받은 accessKey, secretAccessKey는 csv파일로 다운로드 받아 보관. secretAccessKey는 콘솔에서 다시 열람할 수 없다.**


## bucket public access 편집
console 최상위에서 허용을 해야 bucket별, 폴더별, 파일(객체)별 설정이 가능하다. 최상위에서 하지 않고, 하위에서 조작시 public처리가 되지않아 삽질했으니 주의할 것.

![](https://d.pr/i/psHmLw+)



## 첨부파일 S3 업로드
[S3 파일관리 - upload, download, delete 제일 정리 잘된 포스팅](https://charlie-choi.tistory.com/236)

@src/main/resources/template/summernote/form.html 
@src/main/resources/template/summernote/view.html

    <script>
    function sendFile(file, el){

        ...
        $.ajax({
          ...
          //url: ctx+'image',
          url: ctx+'summernote/s3/image',
          ...
          success: function(data) {
            console.log("data =",data);
            $(el).summernote('editor.insertImage', data);
          }
        });
    }
    </script>

@SummernoteController
  
    @Autowired
  	AWSService awsService;

    ...

    @PostMapping("/summernote/s3/image")
    @ResponseBody
    public ResponseEntity<?> handleFileUploadViaS3(@RequestParam("file") MultipartFile file,
    										  HttpServletRequest request) {
		String saveDirectory = request.getSession().getServletContext().getRealPath("/upload");
		
        try {
        
        	S3Object s3obj = awsService.store(saveDirectory, file);
        	logger.debug("s3obj={}", s3obj);
        	
            return ResponseEntity.ok().body(s3obj.getResourceUrl());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

@com.kh.spring.aws.model.vo.S3Object
파일첨부 관련 정보를 가진 vo
`@Data` => A shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, @Setter on all non-final fields, and @RequiredArgsConstructor!
[https://projectlombok.org/features/Data](https://projectlombok.org/features/Data)

**컬럼명과 필드명이 다르다면, 반드시 @Column의 name속성을 명시한다.**


    @Entity
    @Dat팅
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
      
      Date regDate;
    }


@com.kh.spring.aws.model.service.AWSService

  
    public interface AWSService {

      S3Object store(String saveDirectory, MultipartFile file);

    }
  
@com.kh.spring.aws.model.service.AWSServiceImpl
생성자 준비

    @Service
    public class AWSServiceImpl implements AWSService {

      static final Logger logger = LoggerFactory.getLogger(AWSServiceImpl.class);
      
      private AmazonS3 amazonS3;
      
      
      public AWSServiceImpl() {
        //1. accessKey, secretAccessKey 값 읽어오기
        //자바파일상에 값을 명시하지 않고, 별도의 properties파일을 통해 관리
        //(aws_credential.propeties는 버젼관리 하지 않음)
        Properties prop = new Properties();
        String fileName = AWSServiceImpl.class.getResource("/aws_credential.properties")
                            .getPath();
        try {
          prop.load(new FileReader(fileName));
        } catch (IOException e) {
          e.printStackTrace();
        }
        final String accessKey = prop.getProperty("accessKey");
        final String secretAccessKey = prop.getProperty("secretAccessKey");

        //2. AWSCredentials 
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
        
        //3. AmazonS3 
    //		amazonS3 = new AmazonS3Client(awsCredentials);//deprecated
        amazonS3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .withRegion(Regions.AP_NORTHEAST_2) //아시아태평양(서울)
                        .build();

      }

@aws_redentail.properties
IAM에서 사용자 생성하고 발급받은 accessKey, secretAccessKey 등록할 것
필히 .gitignore에 등록할 것!

    #aws_credential.properties
    accessKey=**********************************
    secretAccessKey=**********************************


@com.kh.spring.aws.model.service.AWSServiceImpl
MultipartFile에서 File타입으로 변환하는 메소드는 없다. s3전송을 지원하는 타입은 File타입뿐이므로, 임시파일을 생성해서 업로드후 삭제한다.
[https://stackoverflow.com/questions/24339990/how-to-convert-a-multipart-file-to-file](https://stackoverflow.com/questions/24339990/how-to-convert-a-multipart-file-to-file)

혹은 ObjectMetadata객체를 이용해서 서버에 임시파일을 생성하지 않는 방법이 있다.
[AWS SDK - JAVA를 이용해 S3에 파일 업로드시 로컬에 저장되지 않도록 하기 ](https://galid1.tistory.com/591)


버킷생성은 aws콘솔에서 생성, 테스트후 진행하는 것을 추천. 
동적으로 생성할 이유가 없다. 이미 생성된 bucket이 있다면 오류. 
**bucketName은 s3전체 사용자의 bucket중 유일한 이름이어야 하므로 뻔한 이름 사용금지.**

      @Override
      public S3Object store(String saveDirectory, MultipartFile file) {
        //저장될 파일명 생성
        String renamedFileName = Utils.getRenamedFileName(file);
        //임시저장파일 생성
        File tempFile = new File(saveDirectory,renamedFileName);
        
        
        try {
          //해당경로에 파일 저장
          //java.io.File.File(String parent, String child)
          file.transferTo(tempFile);
        } catch (IllegalStateException | IOException e) {
          e.printStackTrace();
        }
        
        String bucketName = "shqkel1863-summernote";
        
        //버킷 생성(aws콘솔에서 진행 추천): 버킷명은 모두 소문자에 _ 등 특수문자를 포함할 수 없다.
        //최초 1회에 한하여 동적으로 실행가능: com.amazonaws.services.s3.model.AmazonS3Exception: Your previous request to create the named bucket succeeded and you already own it. (Service: Amazon S3; Status Code: 409; Error Code: BucketAlreadyOwnedByYou; Request ID: 5FC9E6E8A84CF496; S3 Extended Request ID: y18m9tS5B2lwjin3wUeauwCe5b/rq0bwWTf2kyks7+Y+aQfL8QLUuPMDLQCxFdj28UbcbUa0vts=), S3 Extended Request ID: y18m9tS5B2lwjin3wUeauwCe5b/rq0bwWTf2kyks7+Y+aQfL8QLUuPMDLQCxFdj28UbcbUa0vts=
    //		amazonS3.createBucket(bucketName);//간단버젼
        
        
        //지역설정하면 오류 유발
        //com.amazonaws.services.s3.model.AmazonS3Exception: The AP_NORTHEAST_2 location constraint is incompatible for the region specific endpoint this request was sent to. (Service: Amazon S3; Status Code: 400; Error Code: IllegalLocationConstraintException; Request ID: 49994E7DE0CDB99D; S3 Extended Request ID: adU0HmtN+Ek1PVkZAp27rSIAJP92/y0SWas/tNprFgwzrXnC0dur48l5iakvnJ9qQtTbZHq3b78=), S3 Extended Request ID: adU0HmtN+Ek1PVkZAp27rSIAJP92/y0SWas/tNprFgwzrXnC0dur48l5iakvnJ9qQtTbZHq3b78=
        //CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName, String.valueOf(Regions.AP_NORTHEAST_2));
        
    //		CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);//자동으로 아시아태평양(서울)로 지정
    //		AccessControlList acl = new AccessControlList();
    //		acl.grantAllPermissions();
    //		createBucketRequest.withAccessControlList(acl);
    //		amazonS3.createBucket(createBucketRequest);
        
        
        
        //파일 업로드
        String key = "images/"+renamedFileName; //버킷내의 파일 경로
        
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, tempFile).withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(putObjectRequest);
        
        //파일 url얻어오기: 파일업로드후 한번에 resourceUrl을 얻을 수 없다.
        //getResourceUrl메소드 사용을 위해 AmazonS3 -> AmazonS3Client 로 downcasting.
        //문자열로 파일경로를 얻기 위해 toString
        String resourceUrl = ((AmazonS3Client)amazonS3).getResourceUrl(bucketName, key);
        
        //db저장을 위해 파일정보를 가진 S3Object객체 생성
        S3Object s3obj = new S3Object();
        s3obj.setOriginalFileName(file.getOriginalFilename());
        s3obj.setRenamedFileName(renamedFileName);
        s3obj.setResourceUrl(resourceUrl);
        s3obj.setContentType(file.getContentType());//MultipartFile.getContentType()
        s3obj.setSize(file.getSize());
        s3obj.setRegDate(new Date());
        
        //임시파일 삭제
        tempFile.delete();
        
        return s3obj;
      }

    }

@com.kh.spring.common.Utils
파일명 재생성하는 static 메소드. s3에서 동일한 파일을 업로드하면 덮어쓰기 처리되므로, 필히 rename후 업로드 한다.
originalFileName과 renamedFileName은 S3Object에서 수집된후 db files테이블에서 별도로 관리하겠음.


    public class Utils {
	
      static final Logger logger = LoggerFactory.getLogger(Utils.class);

      public static String getRenamedFileName(MultipartFile file) {
        
        String originalFileName = file.getOriginalFilename();
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        int rndNum = (int)(Math.random()*1000);
        String renamedFileName = sdf.format(new Date(System.currentTimeMillis()))+"_"+rndNum+ext;
        
        logger.debug("생성된 파일명 = {}", renamedFileName);
        
        return renamedFileName;
      }
    }


### 임시파일 없이 업로드하는 방법

    @Override
    public S3Object storeWithoutTempFile(String saveDirectory, MultipartFile file) {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setContentType(file.getContentType());
      objectMetadata.setContentLength(file.getSize());
      //저장될 파일명 생성
      String renamedFileName = Utils.getRenamedFileName(file);
      //파일 업로드
      String key = "images/"+renamedFileName; //버킷내의 파일 경로
      
      PutObjectRequest putObjectRequest;
      try {
        putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(),objectMetadata);
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(putObjectRequest);
        
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      String resourceUrl = ((AmazonS3Client)amazonS3).getResourceUrl(bucketName, key);
      
      //db저장을 위해 파일정보를 가진 S3Object객체 생성
      S3Object s3obj = new S3Object();
      Summernote dummySummernote = new Summernote();
      dummySummernote.setId(id);
      s3obj.setSummernote(dummySummernote);
      s3obj.setOriginalFileName(file.getOriginalFilename());
      s3obj.setRenamedFileName(renamedFileName);
      s3obj.setResourceUrl(resourceUrl);
      s3obj.setContentType(file.getContentType());//MultipartFile.getContentType()
      s3obj.setSize(file.getSize());
      s3obj.setRegDate(new Date());
      
      //db에 s3object정보 저장
      s3obj = save(s3obj);
      
      return s3obj;
    }




## 파일첨부 최대 사이즈 제한

@application.ytml
* maxFileSize - 파일당 최대 파일 크기. 디폴트 값 : -1 제한없음 => springboot에서 1MB
* maxRequestSize - 파일 한 개의 용량이 아니라 multipart/form-data 요청당 최대 파일 크기이다 (여러 파일 업로드 시 총 크기로 보면 된다) 디폴트 값: -1 제한없음 => springboot에서 10MB
* fileSizeThreshold - 업로드하는 파일이 임시로 파일로 저장되지 않고 메모리에서 바로 스트림으로 전달되는 크기의 한계를 나타낸다. 디폴트 값: 0 => springboot 동일


    #file-upload
    spring: 
      servlet:
        multipart:
          max-file-size: 50MB
          max-request-size: 50MB
          file-size-threshold: 0
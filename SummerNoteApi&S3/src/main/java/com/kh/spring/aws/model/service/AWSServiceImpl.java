package com.kh.spring.aws.model.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.kh.spring.aws.model.repository.AWSRepository;
import com.kh.spring.aws.model.vo.S3Object;
import com.kh.spring.common.Utils;
import com.kh.spring.summernote.model.vo.Summernote;

@Service
public class AWSServiceImpl implements AWSService {

	static final Logger logger = LoggerFactory.getLogger(AWSServiceImpl.class);
	
	private AmazonS3 amazonS3;
	
	private final String bucketName = "shqkel1863-summernote";
	
	@Autowired
	private AWSRepository awsRepository;
	
	
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

	/**
	 * s3파일 업로드를 위해서는 File객체가 필요하다.
	 * 핸들러에서 생성된 MultipartFile객체를 File객체(임시파일)로 변환후 업로드 한다.
	 */
	@Override
	public S3Object store(String saveDirectory, Long id, MultipartFile file) {
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
		String key = "images/"+id+"/"+renamedFileName; //버킷내의 파일 경로
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, tempFile);
		putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
		amazonS3.putObject(putObjectRequest);
		
		//파일 url얻어오기: 파일업로드후 한번에 resourceUrl을 얻을 수 없다.
		//getResourceUrl메소드 사용을 위해 AmazonS3 -> AmazonS3Client 로 downcasting.
		//문자열로 파일경로를 얻기 위해 toString
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
		
		//임시파일 삭제
		tempFile.delete();
		
		//db에 s3object정보 저장
		s3obj = save(s3obj);
		
		return s3obj;
	}
	
	@Override
	public S3Object storeWithoutTempFile(String saveDirectory, Long id, MultipartFile file) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(file.getSize());
		//저장될 파일명 생성
		String renamedFileName = Utils.getRenamedFileName(file);
		//파일 업로드
		String key = "images/"+id+"/"+renamedFileName; //버킷내의 파일 경로
		
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
	

	private S3Object save(S3Object s3obj) {
		s3obj = awsRepository.save(s3obj);
		return s3obj;
	}

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

	@Override
	public void deleteObject(String[] s3keys) {
		Arrays.stream(s3keys)
			  .forEach(key -> {
				  DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
				  amazonS3.deleteObject(deleteObjectRequest);
			  });
	}

	@Override
	public void deleteBySummernoteId(Long id) {
		//db에서 첨부파일 행 제거
		awsRepository.deleteBySummernoteId(id);
		
		deleteObjectBySummernoteId(id);
	}
	
	/**
	 * s3의 폴더는 애초에 존재하지 않는 가상의 것임. 
	 * 내부의 파일이 모두 제거되면, 폴더 역시 제거된다.
	 * 
	 * @param id
	 */
	public void deleteObjectBySummernoteId(Long id) {
			
		ListObjectsRequest listObjectRequest = new ListObjectsRequest();
		listObjectRequest.setBucketName(bucketName);
		listObjectRequest.setPrefix("images/"+id);
		
		ObjectListing objectListing = null;
		do {
			objectListing = amazonS3.listObjects(listObjectRequest);
			
			for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				logger.debug("key = {}", objectSummary.getKey());
				
				String key = objectSummary.getKey();
				DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
				amazonS3.deleteObject(deleteObjectRequest);
				
			}
			//파일을 1000개단위로 가져오며,모든 파일을 가져올수 있다.
			objectListing.setMarker(objectListing.getNextMarker());
			
		} while(objectListing.isTruncated());//objectListing의 완료여부를 리턴함. not complete일때 true.
		
	}
	
	
	

}

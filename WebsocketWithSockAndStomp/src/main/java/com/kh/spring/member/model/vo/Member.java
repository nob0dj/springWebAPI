package com.kh.spring.member.model.vo;

import java.sql.Date;
import java.util.Arrays;

/**
 * VO Value Object : 데이터베이스 테이블 Member의 각 컬럼값 저장용 객체 : 한 행의 정보를 저장
 * DTO Data Tranfer Object
 * DO Domain Object
 * Entity(Strut에서는 이용어를 사용함)
 * bean(EJB에서 사용): 엔터프라이즈 자바빈즈(Enterprise JavaBeans; EJB)는 기업환경의 시스템을 구현하기 위한 서버측 컴포넌트 모델이다. 즉, EJB는 애플리케이션의 업무 로직을 가지고 있는 서버 애플리케이션이다.
 * 
 * VO 조건
 * 1. 모든 필드는 반드시 private여야함.
 * 2. 기본생성자와 매개변수 있는 생성자필요
 * 3. 모든 필드에 대한 getter/setter 필요
 * 4. 직렬화 처리(네트워크상 데이터처리를 위함)
 * 
 * @author nobodj
 *
 */
public class Member implements java.io.Serializable{
	/**
	 * 직렬화/역직렬화
	 * 40BYTE의 객체를 파일입출력단위로 직렬화, 수신측에서 역직렬화시 여러객체를 구분할 고유아이디가 필요하다. 
	 * 없다면, 이후 framework작업에서 에러유발
	 */
	private static final long serialVersionUID = 1L;
	
	private String memberId;
	private String password;
	private String memberName;
	private String gender;		//PreparedStatement에 setCharacter메소드 없음.
	private int age; 
	private String email;
	private String phone;
	private String address;
	private String[] hobby;
	private Date enrollDate;
	
	public Member(){
		
	}

	public Member(String memberId, String password, String memberName, String gender, int age, String email, String phone,
			String address, String[] hobby, Date enrollDate) {
		super();
		this.memberId = memberId;
		this.password = password;
		this.memberName = memberName;
		this.gender = gender;
		this.age = age;
		this.email = email;
		this.phone = phone;
		this.address = address;
		this.hobby = hobby;
		this.enrollDate = enrollDate;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String[] getHobby() {
		return hobby;
	}

	public void setHobby(String[] hobby) {
		this.hobby = hobby;
	}

	public Date getEnrollDate() {
		return enrollDate;
	}

	public void setEnrollDate(Date enrollDate) {
		this.enrollDate = enrollDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Member [memberId=" + memberId + ", password=" + password + ", memberName=" + memberName + ", gender=" + gender
				+ ", age=" + age + ", email=" + email + ", phone=" + phone + ", address=" + address + ", hobby="
				+ Arrays.toString(hobby) + ", enrollDate=" + enrollDate + "]";
	}
	
	
	
}

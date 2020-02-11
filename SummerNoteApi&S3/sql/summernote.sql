--==============================================================
-- summernote테이블 생성
--==============================================================
create table summernote(
    id number,
    writer varchar2(256) not null,
    summernote clob,
    reg_date date default sysdate,
    constraint pk_summernote primary key(id)
);

create sequence seq_summernote;



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
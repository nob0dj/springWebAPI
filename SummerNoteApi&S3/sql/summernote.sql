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
    summernote;

--delete from summernote where is_temp = 'Y';
--commit;

select 
  * 
from 
  s3object
order by id desc;
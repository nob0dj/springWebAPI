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


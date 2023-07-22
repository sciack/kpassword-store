alter table PS_User add userid varchar(255) not null default 'undefined';


update PS_User
set userid = REGEXP_REPLACE(email, '@.*$', '');



update services
set userid = REGEXP_REPLACE(userid, '@.*$', '');

update services_hist
set userid = REGEXP_REPLACE(userid, '@.*$', '');

create unique index UIX_USER_USERID on PS_User(userid);

select * from PS_User


commit;

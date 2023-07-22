alter table services add userid varchar(255) not null default 'undefined';
alter table services_hist add userid varchar(255) not null default  'undefined';

update services 
set userid = 'm.sciachero@gmail.com';

update services_hist 
set userid = 'm.sciachero@gmail.com';

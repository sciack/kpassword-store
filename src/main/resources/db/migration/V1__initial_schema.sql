CREATE TABLE PS_User (
    id bigint NOT NULL AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    password varchar(1024) NOT NULL,
    fullname varchar(255) NOT NULL,
    role varchar(255) NOT NULL,
    PRIMARY KEY (id)
);
 

insert into PS_User(email, password, fullname, role) values ('m.sciachero@gmail.com', 'secret', 'Mirko Sciachero', 'NormalUser');
insert into PS_User(email, password, fullname, role) values ('admin@test.com', 'secret', 'Admin', 'Administrator');

create table services (
	service varchar(255) not null,
	username varchar(255),
	password varchar(1024),
	note     text,
	lastUpdate timestamp,
	primary key(service)
);


create table services_hist(
	id bigint not null auto_increment,
	service varchar(255) not null,
	username varchar(255),
	password varchar(1024),
	note     text,
	lastUpdate timestamp,
	operation varchar(255),
	primary key(id)
);

update PS_User
set password = '80feed1d5c2fcb026d9be972b109ea731f8d1ecf'
where email = 'm.sciachero@gmail.com';

update PS_User
set password = 'd733df8298621972ece9d78ecf4de1743c863f96'
where email = 'admin@test.com';
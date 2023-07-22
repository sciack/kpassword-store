create table services_new (
    id         bigint not null auto_increment,
	service    varchar(255) not null,
	username   varchar(255) not null,
	password   varchar(1024),
	note       text,
	lastUpdate timestamp default current_timestamp,
	userid    varchar(255) not null,
	primary key(id)
);

CREATE UNIQUE INDEX UK_SERVICE_USER on SERVICES_NEW(service, userid);

create table SERVICE_TAGS_NEW (
    id_service  bigint not null,
    id_tag      bigint not null,
    PRIMARY KEY(id_service, id_tag),
    FOREIGN KEY (id_service) REFERENCES SERVICES_NEW(ID),
    FOREIGN KEY (id_tag)     REFERENCES TAGS(ID)
);

insert into services_new (service, username, password, note, lastUpdate, userid)
select service, username, password, note, lastUpdate, userid from services;

insert into service_tags_new (id_service, id_tag)
select s.id, st.id_tag from service_tags st, services_new s
where s.service = st.id_service;

drop table service_tags;

drop table services;

alter table services_new rename to services;

alter table service_tags_new rename to service_tags;

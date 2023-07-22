CREATE TABLE TAGS(
    id bigint NOT NULL AUTO_INCREMENT,
    tag varchar(50) NOT NULL,
    insertTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX UK_TAG ON TAGS(TAG);

create table SERVICE_TAGS (
    id_service varchar(255) not null,
    id_tag      bigint not null,
    PRIMARY KEY(id_service, id_tag),
    FOREIGN KEY (id_service) REFERENCES SERVICES(SERVICE),
    FOREIGN KEY (id_tag)     REFERENCES TAGS(ID)
);

insert into TAGS(TAG) values ('Finance');
insert into TAGS(TAG) values ('Travel');
insert into TAGS(TAG) values ('Cloud');
insert into TAGS(TAG) values ('Technology');
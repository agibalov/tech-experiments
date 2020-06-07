create table Schools(
    id varchar(64) primary key,
    name varchar(256) not null
);

create table Classes(
    id varchar(64) primary key,
    schoolId varchar(64) not null,
    name varchar(256) not null,
    foreign key (schoolId) references Schools(id)
);

create table Students(
    id varchar(64) primary key,
    classId varchar(64) not null,
    name varchar(256) not null,
    foreign key (classId) references Classes(id)
);

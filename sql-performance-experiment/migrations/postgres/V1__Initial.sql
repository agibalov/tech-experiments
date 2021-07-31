create table Accounts(
    id varchar(64) not null primary key,
    name varchar(256) not null
);

create type UserRole as enum('administrator', 'aide', 'guardian', 'parent', 'proctor', 'relative', 'student', 'teacher');

create table Users(
    accountId varchar(64) not null,
    sourcedId varchar(64) not null,
    dateLastModified timestamp not null,
    username varchar(256) not null,
    password varchar(64) not null,
    givenName varchar(256) not null,
    familyName varchar(256) not null,
    role UserRole not null,
    email varchar(256),
    primary key (accountId, sourcedId),
    constraint UserAccountIdFK foreign key (accountId) references Accounts(id),
    constraint UserAccountIdUsernameAK unique (accountId, username),
    constraint UserEmailAK unique (email)
);

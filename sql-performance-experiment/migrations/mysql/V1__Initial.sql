create table Accounts(
    id varchar(64) not null primary key,
    name varchar(256) not null
);

create table Users(
    accountId varchar(64) not null,
    sourcedId varchar(64) not null,
    dateLastModified datetime not null,
    username varchar(256) not null,
    password varchar(64) not null,
    givenName varchar(256) not null,
    familyName varchar(256) not null,
    role enum('administrator', 'aide', 'guardian', 'parent', 'proctor', 'relative', 'student', 'teacher') not null,
    email varchar(256),
    primary key (accountId, sourcedId),
    constraint UserAccountIdFK foreign key (accountId) references Accounts(id),
    constraint UserAccountIdUsernameAK unique (accountId, username),
    constraint UserEmailAK unique (email)
);

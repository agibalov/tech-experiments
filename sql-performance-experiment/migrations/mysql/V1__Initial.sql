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

delimiter $$

create procedure MakeTemplateData(
    in _users int)
begin
    declare _userIndex int;
    declare _userId varchar(64);

    start transaction;

    create temporary table TemplateUsers(
        sourcedId varchar(64) not null,
        dateLastModified datetime not null,
        username varchar(256) not null,
        password varchar(64) not null,
        givenName varchar(256) not null,
        familyName varchar(256) not null,
        role enum('administrator', 'aide', 'guardian', 'parent', 'proctor',
            'relative', 'student', 'teacher') not null,
        email varchar(256)
    );

    set _userIndex = 0;
    while _userIndex < _users do
        set _userId = uuid();
        insert into TemplateUsers(sourcedId, dateLastModified, username, password,
            givenName, familyName, role, email)
        values (_userId, '2021-01-01 00:00:00', concat('u-', _userId), _userId,
            concat('Given ', _userId), concat('Family ', _userId), 'student', null);

        set _userIndex = _userIndex + 1;
    end while;

    commit;
end $$

create procedure DeleteTemplateData()
begin
    drop table TemplateUsers;
end $$

create procedure MakeAccount()
begin
    declare _accountId varchar(64);
    set _accountId = uuid();

    start transaction;

    insert into Accounts(id, name)
    values (_accountId, concat('Account ', _accountId));

    insert into Users(accountId, sourcedId, dateLastModified, username, password,
        givenName, familyName, role, email)
    select _accountId, sourcedId, dateLastModified, username, password,
        givenName, familyName, role, email
    from TemplateUsers;

    commit;
end $$

delimiter ;

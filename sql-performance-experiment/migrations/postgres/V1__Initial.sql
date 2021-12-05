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

create procedure MakeTemplateData(users integer)
language plpgsql
as $$
declare
    userId varchar(64);
begin
    create temporary table TemplateUsers(
        sourcedId varchar(64) not null,
        dateLastModified timestamp not null,
        username varchar(256) not null,
        password varchar(64) not null,
        givenName varchar(256) not null,
        familyName varchar(256) not null,
        role UserRole not null,
        email varchar(256)
    );

    for i in 1..users loop
        userId = gen_random_uuid();
        insert into TemplateUsers(sourcedId, dateLastModified, username, password,
            givenName, familyName, role, email)
        values (userId, '2021-01-01 00:00:00', concat('u-', userId), userId,
            concat('Given ', userId), concat('Family ', userId), 'student', null);
    end loop;
end $$;

create procedure DeleteTemplateData()
language plpgsql
as $$
begin
    drop table TemplateUsers;
end $$;

create procedure MakeAccount()
language plpgsql
as $$
declare
    accountId varchar(64);
begin
    accountId = gen_random_uuid();

    insert into Accounts(id, name)
    values (accountId, concat('Account ', accountId));

    insert into Users(accountId, sourcedId, dateLastModified, username, password,
        givenName, familyName, role, email)
    select accountId, sourcedId, dateLastModified, username, password,
        givenName, familyName, role, email
    from TemplateUsers;
end $$;

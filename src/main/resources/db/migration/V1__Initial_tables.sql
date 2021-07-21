create table LINK_ALREADY_PROCESSED(
    link varchar(2000)
);

create table LINK_TO_BE_PROCESSED(
    link varchar(2000)
);

create table news (
    id bigint primary key auto_increment,
    title text,
    content text,
    url varchar(2000),
    created_at timestamp default now(),
    modified_at timestamp default now()
);
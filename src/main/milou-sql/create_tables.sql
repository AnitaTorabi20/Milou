create table user (
    id int auto_increment primary key,
    username varchar(255) not null unique,
    password varchar(255) not null,
    email varchar(255) not null unique
);

create table folder (
    id int auto_increment primary key,
    name varchar(100) not null,
    user_id int,
    foreign key (user_id) references user(id) on delete cascade
);

create table email (
    id int auto_increment primary key,
    subject varchar(255),
    body text,
    sent_time datetime,
    sender_id int,
    sender varchar(255),
    folder_id int,
    code varchar(20),
    IsRead boolean default 0,
    recipient varchar(255),
    deleted boolean default false,
    foreign key (sender_id) references user(id) on delete cascade,
    foreign key (folder_id) references folder(id) on delete set null
);

create table recipient_email (
    email_id int,
    recipient_id int,
    primary key (email_id, recipient_id),
    foreign key (email_id) references email(id) on delete cascade,
    foreign key (recipient_id) references user(id) on delete cascade
);

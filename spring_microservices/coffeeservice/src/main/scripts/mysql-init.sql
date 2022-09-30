drop database if exists coffeeservice;
drop user if exists `coffee_service`@`%`;

create database if not exists coffeeservice character set utf8mb4 collate utf8mb4_unicode_ci;
create user if not exists `coffee_service`@`%` identified with mysql_native_password by 'password';
grant select, insert, update, delete, create, drop, references, index, alter, execute, create view, show view,
    create routine, alter routine, event, trigger on `coffeeservice`.* to `coffee_service`@`%`;
flush privileges;
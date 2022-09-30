drop database if exists orderservice;
drop user if exists `order_service`@`%`;

create database if not exists orderservice character set utf8mb4 collate utf8mb4_unicode_ci;
create user if not exists `order_service`@`%` identified with mysql_native_password by 'password';
grant select, insert, update, delete, create, drop, references, index, alter, execute, create view, show view,
    create routine, alter routine, event, trigger on `orderservice`.* to `order_service`@`%`;
flush privileges;
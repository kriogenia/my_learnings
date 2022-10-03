drop database if exists inventoryservice;
drop user if exists `inventory_service`@`%`;

create database if not exists inventoryservice character set utf8mb4 collate utf8mb4_unicode_ci;
create user if not exists `inventory_service`@`%` identified with mysql_native_password by 'password';
grant select, insert, update, delete, create, drop, references, index, alter, execute, create view, show view,
    create routine, alter routine, event, trigger on `inventoryservice`.* to `inventory_service`@`%`;
flush privileges;
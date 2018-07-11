-- Производимые товары
create table products
(
    ID integer AUTO_INCREMENT PRIMARY KEY,
    NAME varchar(80) UNIQUE NOT NULL
);

-- Материалы на складе
create table materials
(
    ID integer AUTO_INCREMENT PRIMARY KEY,
    NAME varchar(80) UNIQUE NOT NULL,
    AMOUNT integer NOT NULL,
    PRICE bigint NOT NULL
);

-- Зависимость товаров от материалов
create table relation
(
    PID integer NOT NULL,
    MID integer NOT NULL,
    AMOUNT integer NOT NULL
);

-- Статистика запросов по клиенту
create table customer
(
    NAME varchar(30) NOT NULL,
    PID integer NOT NULL,
    AMOUNT integer NOT NULL,
    SALE bit NOT NULL,
    PRICE bigint NOT NULL
);

-- Статистика запросов по поставщику
create table provider
(
    NAME varchar(30) NOT NULL,
    MID integer NOT NULL,
    AMOUNT integer NOT NULL,
    SALE bit NOT NULL,
    PRICE bigint NOT NULL
);

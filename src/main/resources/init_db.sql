drop table if exists ORDER_ITEM;
drop table if exists "ORDER";
drop table if exists PRODUCT;

create table PRODUCT
(
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    quantity   INTEGER NOT NULL,
    price DECIMAL(20,2),
    status VARCHAR(20) NOT NULL default 'ACTIVE',
    PRIMARY KEY (id)
  
);

insert into PRODUCT (name, quantity, price) values('Beer', 100, 9.99);
insert into PRODUCT (name, quantity, price) values('Whisky', 1000, 12.01);
insert into PRODUCT (name, quantity, price) values('Milk', 10, 4.99);
insert into PRODUCT (name, quantity, price) values('Water', 2000, 0.99);


create table "ORDER"
(
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE default CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
insert into "ORDER" (status) values('CREATED');
insert into "ORDER" (status) values('CANCELED');
insert into "ORDER" (status) values('PAID');
insert into "ORDER" (status) values('FINISHED');


create table ORDER_ITEM
(
    id       INTEGER NOT NULL AUTO_INCREMENT,
    order_id INTEGER NOT NULL,
    product_id  INTEGER NOT NULL,
    quantity   INTEGER NOT NULL,
    foreign key (order_id) references "ORDER" (ID),
    foreign key (product_id) references PRODUCT (ID),
    PRIMARY KEY (id)
);
insert into ORDER_ITEM (order_id, product_id, quantity) values(1, 1, 5);
insert into ORDER_ITEM (order_id, product_id, quantity) values(1, 2, 10);
insert into ORDER_ITEM (order_id, product_id, quantity) values(1, 3, 15);
insert into ORDER_ITEM (order_id, product_id, quantity) values(2, 4, 5);
insert into ORDER_ITEM (order_id, product_id, quantity) values(3, 4, 6);
insert into ORDER_ITEM (order_id, product_id, quantity) values(4, 4, 7);

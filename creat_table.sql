-- 用户表
create table if not exists `service_employee`
(
    `id` bigint  comment '主键' primary key,
    `name` varchar(256) not null comment '用户名',
    `email` varchar(20) not null comment '电子邮件',
    `avatar` varchar(256) not null comment '头像',
    `position` tinyint not null comment '职位 ',
    `shopId` bigint not null comment '门店id',
    `pwd` varchar(20) not null comment '密码',
    `is_deleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户表';

insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (730, '严立果', 'floy.sanford@hotmail.com', 'www.victor-shanahan.info', '经理', 8000, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (3579293601, '贾鹏', 'jonas.heidenreich@gmail.com', 'www.britni-turner.org', '副经理', 8001, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (3875604002, '黎明辉', 'prince.volkman@yahoo.com', 'www.guillermina-kemmer.net', '副经理', 8002, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (5374, '黎涛', 'luis.harvey@gmail.com', 'www.gerry-blick.net', '小组长', 8003, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (25195639, '曾明杰', 'randolph.daugherty@hotmail.com', 'www.donya-brekke.biz', '小组长', 8004, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (51, '姚皓轩', 'rhonda.cruickshank@gmail.com', 'www.erasmo-casper.com', '店员', 8005, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (61797929, '梁修洁', 'marion.walter@gmail.com', 'www.edmund-spinka.net', '店员', 8006, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (31, '董伟祺', 'jan.volkman@hotmail.com', 'www.patrick-schmidt.biz', '店员', 8007, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (3, '邓峻熙', 'tanna.kling@gmail.com', 'www.sherwood-beahan.net', '店员', 8008, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (371309, '萧瑞霖', 'percy.leffler@yahoo.com', 'www.deidra-wiegand.name', '店员', 8009, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (29, '王文昊', 'jimmy.hettinger@gmail.com', 'www.leonel-vonrueden.biz', '店员', 8010, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (12, '邱志泽', 'junko.rath@gmail.com', 'www.dorian-cormier.name', '店员', 8011, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (462927, '曾思远', 'augusta.bayer@gmail.com', 'www.mollie-ohara.org', '店员', 8012, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (693, '阎伟诚', 'glen.lesch@gmail.com', 'www.georgina-beier.name', '店员', 8013, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (808, '田振家', 'cletus.rogahn@hotmail.com', 'www.boris-deckow.co', '店员', 8014, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (26598, '邵立诚', 'romaine.emmerich@hotmail.com', 'www.delbert-schowalter.biz', '店员', 8015, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (99504770, '廖皓轩', 'margarita.gislason@yahoo.com', 'www.ima-lebsack.net', '店员', 8016, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (874069863, '傅旭尧', 'jeanine.yost@yahoo.com', 'www.kelvin-rodriguez.co', '店员', 8017, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (2, '沈修杰', 'jamar.runolfsdottir@yahoo.com', 'www.tanja-nader.biz', '店员', 8018, '202CB962AC59075B964B07152D234B70');
insert into `service_employee` (`id`, `name`, `email`, `avatar`, `position`, `shopId`, `pwd`) values (1, '秦思聪', 'louie.kohler@gmail.com', 'www.kurtis-beer.io', '店员', 8019, '202CB962AC59075B964B07152D234B70');


-- 门店信息表
create table if not exists `service_shop`
(
    `id` bigint comment '主键' primary key,
    `shop_name` varchar(256) not null comment '用户名',
    `address` varchar(256) not null comment '门店地址',
    `size` int not null comment '面积（平方米）',
    `is_deleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '门店信息表';

insert into `service_shop` (`id`, `shop_name`, `address`, `size`) values (8000, '联想', '绵阳市', 9);
insert into `service_shop` (`id`, `shop_name`, `address`, `size`) values (8001, '华为', '五常市', 8768823);
insert into `service_shop` (`id`, `shop_name`, `address`, `size`) values (8002, '中兴', '扬中市', 51918);




-- 客流量信息表
create table if not exists `Passenger_flow`
(
    `id` bigint not null comment 'id' primary key,
    `shopId` bigint not null comment '门店id',
    `Date` Datetime not null comment '日期',
    `start_time` time not null comment '开始时间',
    `end_time` time not null comment '结束时间',
    `forecast_passenger` float not null comment '预测客流量'
) comment '客流量信息表';


-- 员工偏好表
create table if not exists `Employee_preference`
(
    `EmployeeId` bigint not null auto_increment comment '员工id' primary key,
    `preference_type_id` bigint not null comment '偏好类型id',
    `preference_value` varchar(256) not null comment '偏好值',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '员工偏好表';


-- 偏好表
create table if not exists `preference`
(
    `id` bigint not null auto_increment comment 'id' primary key,
    `preference_type` varchar(256) not null comment '偏好类型',
    `optional_preference` varchar(256) not null comment '可选偏好值'
) comment '偏好表';


-- 员工班次信息表
create table if not exists `Employee_classes`
(
    `EmployeeId` bigint not null comment '员工id',
    `classesId` bigint not null comment '班次id'
) comment '员工班次信息表';


-- 班次信息表
create table if not exists `classes`
(
    `id` bigint not null auto_increment comment 'id' primary key,
    `start_time` time not null comment '开始时间',
    `end_time` time not null comment '结束时间',
    `employee_number` smallint not null comment '该时间段店员数量'
) comment '班次信息表';


-- 门店规则表
create table if not exists `scheduling_rules`
(
    `id` bigint not null comment 'id',
    `shop_id` bigint not null comment '门店id',
    `rule_id` bigint not null comment '规则id',
    `rule_value` json not null comment '规则默认值'
) comment '门店规则表';



-- 排班表
create table if not exists `scheduling`
(
    `id` varchar(255) primary key,
    `sign` varchar(256) not null comment '标志',
    `employee_id` varchar(255) not null comment '员工id',
    `rosterString` varchar(256) not null comment '排班字符串',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '排班表';

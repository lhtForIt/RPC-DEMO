//账户表
CREATE TABLE `t_bank_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 自增列',
  `customer_id` int(11) NOT NULL COMMENT '用户编号',
  `account_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '账户类型：1 人民币账户，2 美元账户',
  `balance` bigint(20) NOT NULL COMMENT '客户余额 单位 分',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


//冻结资产表
CREATE TABLE `t_bank_freeze` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 自增列',
  `customer_id` int(11) NOT NULL COMMENT '用户编号',
  `account_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '账户类型：1 人民币账户，2 美元账户',
  `balance` bigint(20) NOT NULL COMMENT '客户余额 单位 分',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


//tcc监控表
CREATE TABLE `t_try_log` (
  `tx_no` varchar(64) NOT NULL COMMENT '事务id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tx_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `t_confirm_log` (
                                 `tx_no` varchar(64) NOT NULL COMMENT '事务id',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`tx_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


create database hmily DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;



CREATE TABLE `t_cancel_log` (
                                `tx_no` varchar(64) NOT NULL COMMENT '事务id',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`tx_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

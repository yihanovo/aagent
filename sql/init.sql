-- ============================================================
-- 无忧医疗问诊智能体 - 数据库初始化脚本
-- 使用方法：mysql -u root -p < sql/init.sql
-- 脚本可重复执行（建表 IF NOT EXISTS，种子数据按唯一键幂等写入）
-- ============================================================

CREATE DATABASE IF NOT EXISTS guiguxiaozhi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE guiguxiaozhi;

-- 预约挂号表（由 Agent 通过 Function Calling 写入）
CREATE TABLE IF NOT EXISTS appointment (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    username    VARCHAR(64)  NOT NULL                COMMENT '患者姓名',
    id_card     VARCHAR(32)  NULL                    COMMENT '身份证号',
    department  VARCHAR(64)  NULL                    COMMENT '科室名称',
    `date`      VARCHAR(32)  NULL                    COMMENT '预约日期',
    `time`      VARCHAR(16)  NULL                    COMMENT '时间段：上午/下午',
    doctor_name VARCHAR(64)  NULL                    COMMENT '医生姓名',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '预约挂号记录';

-- 医生排班表（号源查询的数据来源）
-- 某时段剩余号源 = capacity - appointment 表中同时段的已预约数量
CREATE TABLE IF NOT EXISTS doctor_schedule (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    department  VARCHAR(64)  NOT NULL                COMMENT '科室名称',
    doctor_name VARCHAR(64)  NOT NULL                COMMENT '医生姓名',
    `date`      VARCHAR(32)  NOT NULL                COMMENT '排班日期（yyyy-MM-dd）',
    `time`      VARCHAR(16)  NOT NULL                COMMENT '时间段：上午/下午',
    capacity    INT          NOT NULL DEFAULT 20     COMMENT '该时段总号源数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule (department, doctor_name, `date`, `time`)
) ENGINE = InnoDB COMMENT = '医生排班与号源容量';

-- 种子排班数据：以执行日为基准，向后 1~3 天。
-- 用 DATE_ADD(CURDATE(), INTERVAL n DAY) 保证种子数据不会过期。
INSERT INTO doctor_schedule (department, doctor_name, `date`, `time`, capacity) VALUES
    ('神经内科', '张三', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', 20),
    ('神经内科', '张三', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', 15),
    ('神经内科', '李四', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', 20),
    ('神经内科', '李四', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '下午', 15),
    ('心内科',   '王五', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', 20),
    ('心内科',   '王五', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '下午', 15),
    ('儿科',     '赵六', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', 20),
    ('儿科',     '赵六', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', 15),
    ('消化内科', '钱七', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', 20),
    ('消化内科', '钱七', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '下午', 15)
ON DUPLICATE KEY UPDATE capacity = VALUES(capacity);

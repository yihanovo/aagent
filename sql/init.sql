-- ============================================================
-- 小智医疗问诊智能体 - 数据库初始化脚本
-- 使用方法：mysql -u root -p < sql/init.sql
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

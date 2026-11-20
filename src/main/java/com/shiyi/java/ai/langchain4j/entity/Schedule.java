package com.shiyi.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生排班表：某医生在某科室、某日期、某半天时段的出诊容量。
 * 号源是否可约 = 该排班容量 - 同一时间段已存在的预约数量（动态计算）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("doctor_schedule")
public class Schedule {
    //唯一标识，映射到数据库的主键，使用自增策略
    @TableId(type = IdType.AUTO)
    private Long id;
    private String department;
    private String doctorName;
    private String date;
    private String time;
    private Integer capacity;
}

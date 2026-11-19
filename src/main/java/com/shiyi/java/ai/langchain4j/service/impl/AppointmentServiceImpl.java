package com.shiyi.java.ai.langchain4j.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.java.ai.langchain4j.entity.Appointment;
import com.shiyi.java.ai.langchain4j.mapper.AppointmentMapper;
import com.shiyi.java.ai.langchain4j.service.AppointmentService;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {
    /**
     * 查询订单是否存在
     *
     * @param appointment
     * @return
     */
    @Override
    public Appointment getOne(Appointment appointment) {
        // 查询条件构建器，LambdaQueryWrapper 是 MyBatis-Plus 提供的一个用于构建查询条件的工具类，它允许你使用 lambda 表达式来指定查询条件，从而避免了硬编码字段名，提高了代码的可读性和可维护性。
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getUsername, appointment.getUsername());//eq 方法用于添加一个等于条件，这里是查询 username 字段等于 appointment.getUsername() 的记录。
        queryWrapper.eq(Appointment::getIdCard, appointment.getIdCard());
        queryWrapper.eq(Appointment::getDepartment, appointment.getDepartment());
        queryWrapper.eq(Appointment::getDate, appointment.getDate());
        queryWrapper.eq(Appointment::getTime, appointment.getTime());
        Appointment appointmentDB = baseMapper.selectOne(queryWrapper);
        return appointmentDB;
    }

    //TODO:完成可以实现用户提供自己姓名、或者其他可加可不加的信息如预约时间，医生姓名就可以查询预约记录，或者删除预约记录
}
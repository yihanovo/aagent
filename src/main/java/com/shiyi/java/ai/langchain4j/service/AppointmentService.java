package com.shiyi.java.ai.langchain4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.java.ai.langchain4j.entity.Appointment;

public interface AppointmentService extends IService<Appointment> {
    Appointment getOne(Appointment appointment);
}
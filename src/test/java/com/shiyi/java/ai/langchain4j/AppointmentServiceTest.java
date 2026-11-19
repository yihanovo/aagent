package com.shiyi.java.ai.langchain4j;

import com.shiyi.java.ai.langchain4j.entity.Appointment;
import com.shiyi.java.ai.langchain4j.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppointmentServiceTest {
    @Autowired
    private AppointmentService appointmentService;
    //测试查询
    @Test
    void testGetOne() {
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("123456789012345678");
        appointment.setDepartment("内科");
        appointment.setDate("2025-04-14");
        appointment.setTime("上午");
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if (appointmentDB != null) {
            System.out.println(appointmentDB);
        } else {
            System.out.println("未找到匹配的预约记录");
        }
    }

    //测试保存
    @Test
    void testSave() {
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("123456789012345678");
        appointment.setDepartment("内科");
        appointment.setDate("2025-04-14");
        appointment.setTime("上午");
        appointment.setDoctorName("张医生");
        appointmentService.save(appointment);
        System.out.println("保存结果: " + appointment);
    }
    //测试删除
    @Test
    void testRemoveById() {
        appointmentService.removeById(1L);
    }
}
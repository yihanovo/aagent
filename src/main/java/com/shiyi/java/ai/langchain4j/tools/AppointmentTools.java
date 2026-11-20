package com.shiyi.java.ai.langchain4j.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyi.java.ai.langchain4j.entity.Appointment;
import com.shiyi.java.ai.langchain4j.entity.Schedule;
import com.shiyi.java.ai.langchain4j.mapper.ScheduleMapper;
import com.shiyi.java.ai.langchain4j.service.AppointmentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


//AppointmentTools 类是一个工具类，提供了与预约挂号相关的功能。它使用了 Spring 的 @Component 注解，使其成为一个 Spring Bean，可以被自动扫描和注入到其他组件中。该类包含三个主要方法：bookAppointment、cancelAppointment 和 queryDepartment，分别用于预约挂号、取消预约和查询是否有号源。每个方法都使用了 @Tool 注解，提供了方法的名称和描述，以便在 LangChain4j 中被识别和调用。
@Component
public class AppointmentTools {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private ScheduleMapper scheduleMapper;

    //bookAppointment 方法用于处理预约挂号的逻辑。它首先调用 appointmentService.getOne 方法查询数据库中是否存在与传入的 Appointment 对象匹配的预约记录。如果没有找到匹配的记录，则将 appointment 对象的 ID 设置为 null（防止大模型幻觉设置了 ID），然后调用 appointmentService.save 方法保存新的预约记录。如果保存成功，返回 "预约成功，并返回预约详情"；如果保存失败，返回 "预约失败"。如果已经存在相同科室和时间的预约记录，则返回 "您在相同的科室和时间已有预约"。
    @Tool(name="预约挂号", value = "根据参数，先执行工具方法queryDepartment查询是否可预约，并直接给用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约。")
    public String bookAppointment(Appointment appointment){
//查找数据库中是否包含对应的预约记录
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if(appointmentDB == null){
            appointment.setId(null);//防止大模型幻觉设置了id，id是数据库自增的，不需要用户输入
            if(appointmentService.save(appointment)){
                return "预约成功，并返回预约详情";
            }else{
                return "预约失败";
            }
        }
        return "您在相同的科室和时间已有预约";
    }

    //cancelAppointment 方法用于处理取消预约的逻辑。它首先调用 appointmentService.getOne 方法查询数据库中是否存在与传入的 Appointment 对象匹配的预约记录。如果找到了匹配的记录，则调用 appointmentService.removeById 方法删除该预约记录，并根据删除结果返回 "取消预约成功" 或 "取消预约失败"。如果没有找到匹配的预约记录，则返回 "您没有预约记录，请核对预约科室和时间"。
    @Tool(name="取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment){
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if(appointmentDB != null){
//删除预约记录
            if(appointmentService.removeById(appointmentDB.getId())){
                return "取消预约成功";
            }else{
                return "取消预约失败";
            }
        }
//取消失败
        return "您没有预约记录，请核对预约科室和时间";
    }

    //queryDepartment 方法用于查询是否有号源。逻辑基于 doctor_schedule 排班表：
    //先查该科室、日期、时间段下（可带医生名过滤）的排班记录；
    //对每条排班，用"容量 - 同一时间段已约数量"动态计算剩余号源；
    //未指定医生时只要任一医生有剩余即可预约，指定医生时只看该医生的剩余。
    @Tool(name = "查询是否有号源", value = "根据科室名称、日期、时间和医生查询是否有号源。返回值是给用户的查询结果描述（是否有号、剩余数量），请直接转述给用户")
    public String queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        //查询该时段的排班记录（指定了医生则同时按医生过滤）
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getDepartment, name);
        scheduleWrapper.eq(Schedule::getDate, date);
        scheduleWrapper.eq(Schedule::getTime, time);
        if (doctorName != null && !doctorName.isBlank()) {
            scheduleWrapper.eq(Schedule::getDoctorName, doctorName);
        }
        List<Schedule> schedules = scheduleMapper.selectList(scheduleWrapper);

        if (schedules.isEmpty()) {
            return "该" + name + "在" + date + time + "没有排班，暂无法预约";
        }

        //逐条排班计算剩余号源：容量 - 同一科室/日期/时段/医生的已预约数量
        int totalRemaining = 0;
        List<String> doctorsWithSlots = new ArrayList<>();
        for (Schedule schedule : schedules) {
            LambdaQueryWrapper<Appointment> bookedWrapper = new LambdaQueryWrapper<>();
            bookedWrapper.eq(Appointment::getDepartment, name);
            bookedWrapper.eq(Appointment::getDate, date);
            bookedWrapper.eq(Appointment::getTime, time);
            bookedWrapper.eq(Appointment::getDoctorName, schedule.getDoctorName());
            long booked = appointmentService.count(bookedWrapper);

            int remaining = schedule.getCapacity() - (int) booked;
            if (remaining > 0) {
                totalRemaining += remaining;
                doctorsWithSlots.add(schedule.getDoctorName() + "（剩余" + remaining + "个号）");
            }
        }

        if (totalRemaining == 0) {
            return "很抱歉，" + name + "在" + date + time + "的号源已约满";
        }
        return name + "在" + date + time + "有号源，可预约医生：" + String.join("、", doctorsWithSlots)
                + "，共剩余" + totalRemaining + "个号";
    }
}

package com.shiyi.java.ai.langchain4j.tools;

import com.shiyi.java.ai.langchain4j.entity.Appointment;
import com.shiyi.java.ai.langchain4j.service.AppointmentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


//AppointmentTools 类是一个工具类，提供了与预约挂号相关的功能。它使用了 Spring 的 @Component 注解，使其成为一个 Spring Bean，可以被自动扫描和注入到其他组件中。该类包含三个主要方法：bookAppointment、cancelAppointment 和 queryDepartment，分别用于预约挂号、取消预约和查询是否有号源。每个方法都使用了 @Tool 注解，提供了方法的名称和描述，以便在 LangChain4j 中被识别和调用。
@Component
public class AppointmentTools {
    @Autowired
    private AppointmentService appointmentService;
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
    @Tool(name="取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取 消预约成功，否则返回取消预约失败")
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

    //queryDepartment 方法用于查询是否有号源。它接收科室名称、日期、时间和医生名称作为参数，并打印这些参数的值。根据注释中的 TODO 提示，方法的逻辑应该包括维护医生的排班信息：如果没有指定医生名字，则根据其他条件查询是否有可以预约的医生；如果指定了医生名字，则判断医生是否有排班，并进一步判断医生排班时间段是否已约满。当前方法返回 true���表示有号源，但实际逻辑需要根据具体业务需求进行实现。
    @Tool(name = "查询是否有号源", value="根据科室名称，日期，时间和医生查询是否有号源，并返回给用户")
    public boolean queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
//TODO 维护医生的排班信息：
//如果没有指定医生名字，则根据其他条件查询是否有可以预约的医生（有返回true，否则返回false）；
//如果指定了医生名字，则判断医生是否有排班（没有排版返回false）
//如果有排班，则判断医生排班时间段是否已约满（约满返回false，有空闲时间返回true）
        return true;
    }
}

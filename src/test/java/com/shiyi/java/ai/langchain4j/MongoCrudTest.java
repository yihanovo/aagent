package com.shiyi.java.ai.langchain4j;

import com.shiyi.java.ai.langchain4j.bean.ChatMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@SpringBootTest
public class MongoCrudTest {
    @Autowired
    private MongoTemplate mongoTemplate;
/**
 * 插入文档
 */
/* @Test
public void testInsert() {
mongoTemplate.insert(new ChatMessages(1L, "聊天记录"));
}*/
    /**
     * 插入文档
     */
    @Test
    public void testInsert2() {
        ChatMessages chatMessages = new ChatMessages();
        chatMessages.setContent("聊天记录列表");
        mongoTemplate.insert(chatMessages);
    }
    /**
     * 根据id查询文档
     */
    @Test
    public void testFindById() {
        ChatMessages chatMessages = mongoTemplate.findAll(ChatMessages.class).get(0);
        System.out.println(chatMessages);
    }
    /**
     * 修改文档
     */
    @Test
    public void testUpdate() {
        //Criteria是用来定义查询条件的类，where方法指定字段名，is方法指定字段值，query方法将查询条件封装成Query对象，Update类用来定义更新操作，set方法指定要更新的字段名和新值，最后调用mongoTemplate的upsert方法执行更新操作，如果文档不存在则会插入一条新文档
        Criteria criteria = Criteria.where("_id").is("6afd3e5591ccd81c43d91b75");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "新的聊天记录列表");
//修改或新增,upsert中三个参数分别是查询条件、更新操作和要操作的文档类型
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }
    /**
     * 新增或修改文档
     */
    @Test
    public void testUpdate2() {
        Criteria criteria = Criteria.where("_id").is("100");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "新的聊天记录列表");
//修改或新增
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }
    /**
     * 删除文档
     */
    @Test
    public void testDelete() {
        Criteria criteria = Criteria.where("_id").is("100");
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }
}
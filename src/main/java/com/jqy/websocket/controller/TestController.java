package com.jqy.websocket.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhengc
 * @Date: 2019/5/22 10:13
 * @Version 1.0
 * @Note
 */
@RestController
@Slf4j
public class TestController {
    // 收到消息记数
    private AtomicInteger count = new AtomicInteger(0);


    /**
     * @param requestMessage
     * @return 这个方法即使服务端接收客户端消息的方法，也是服务端发送消息给客户端的方法。这两个注解写在一起的效果就是，服务端接收到消息时，会直接
     * 将结果返回给监听的客户端。也就是 通过/receive接收的消息，在这个方法执行到return时，会将结果发送给/topic/getResponse这个路径
     * @MessageMapping 表示客户端发送消息给服务端时，服务端的接收消息路径 （registry.setApplicationDestinationPrefixes("/app");
     * 如果有这个前缀配置，客户端还需要拼接上前缀地址，即/app/receive）
     * @SendTo默认 表示服务端发送消息给客户端时的路径 （对应 registry.enableSimpleBroker("/topic","/queue");里的配置）
     * 特别注意的是，这个注解只能和@MessageMapping配合使用，单独用是没有效果的！！
     * @SendTo 为广播，所有socket连接的客户端都可以接收消息
     * @SendToUser 为独播，只有消息发送给可服务器的客户端可以接收到消息
     */
    @MessageMapping("/receive")
    @SendTo("/topic/getResponse")
//    @SendToUser("/topic/getResponse")
    public JSONObject broadcast(String requestMessage) {
        log.info("receive message = {}", requestMessage);
        log.info("count is {}", count.incrementAndGet());
        JSONObject res = new JSONObject();
        res.put("data", "message is get " + requestMessage);
        return res;
    }

    /**
     * 单纯接收客户端消息
     *
     * @return
     * @throws Exception
     */
    @MessageMapping("/topic/callback")
    public JSONObject callback() throws Exception {
        log.info("count is {}", count.incrementAndGet());
        // 向客户端发送消息
        JSONObject res = new JSONObject();
        res.put("data", "一个传说");
        return res;
    }


    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 测试主动发起客户端推送
     * 所有建立socket连接的客户端都可以收到消息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/test")
    public void test() throws Exception {
        JSONObject res = new JSONObject();
        res.put("data", "你个神经病");
        this.simpMessagingTemplate.convertAndSend("/topic/getResponse", res);//这句话是广播
        //这句话是独播（独播会自动拼接前缀/user,客户端需要监听的路径就变成了/user/topic/getResponse）
//        this.simpMessagingTemplate.convertAndSendToUser("mytest","/topic/getResponse", res);

    }
}

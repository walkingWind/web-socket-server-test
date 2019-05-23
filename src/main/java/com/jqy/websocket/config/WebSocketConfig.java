package com.jqy.websocket.config;

import com.jqy.websocket.domain.MyPrincipal;
import com.jqy.websocket.interceptor.WebSocketChannelInterceptor;
import com.jqy.websocket.interceptor.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * @Author: zhengc
 * @Date: 2019/5/22 10:19
 * @Version 1.0
 * @Note
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private WebSocketInterceptor webSocketInterceptor;
    @Autowired
    private WebSocketChannelInterceptor webSocketChannelInterceptor;
//    @Bean
//    public WebSocketChannelInterceptor createWebSocketChannelInterceptor(){
//        return new WebSocketChannelInterceptor();
//    }


    /**
     * 这个方法指定了创建连接的协议端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /**
         * 注册 Stomp的端点
         * addEndpoint：添加STOMP协议的端点。这个HTTP URL是供WebSocket或SockJS客户端访问的地址
         * withSockJS：指定端点使用SockJS协议
         */
        registry.addEndpoint("/websocket-simple")
                .setAllowedOrigins("*") // 添加允许跨域访问
                .addInterceptors(webSocketInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        //key就是服务器和客户端保持一致的标记，一般可以用账户名称，或者是用户ID。
                        return new MyPrincipal("mytest");
                    }
                })//配置独播
                .withSockJS();
    }

    /**
     * 这个方法指定了一些发送消息和接收消息的路径
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //配置服务端发送路径前缀，对应注解@SendTo (这个路径与下面的互斥，表示服务端发消息个客户端的路径，由客户端监听)
        registry.enableSimpleBroker("/topic", "/queue");
        /**
         * 配置服务端接收路径前缀，对应注解@MessageMapping 表示客户端给服务端发送消息时，服务端接收的路径前缀，
         * 如：@MessageMapping("/h1")，则实际路径是/app/h1
         */
        registry.setApplicationDestinationPrefixes("/app");
    }


    /**
     * 设置输入消息通道的线程数，默认线程为1，可以自己自定义线程数，最大线程数，线程存活时间
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        /*
         * 配置消息线程池
         * 1. corePoolSize 配置核心线程池，当线程数小于此配置时，不管线程中有无空闲的线程，都会产生新线程处理任务
         * 2. maxPoolSize 配置线程池最大数，当线程池数等于此配置时，不会产生新线程
         * 3. keepAliveSeconds 线程池维护线程所允许的空闲时间，单位秒
         */
        registration.taskExecutor().corePoolSize(10)
                .maxPoolSize(20)
                .keepAliveSeconds(60);
        /*
         * 添加stomp自定义拦截器，可以根据业务做一些处理
         * springframework 4.3.12 之后版本此方法废弃，代替方法 interceptors(ChannelInterceptor... interceptors)
         * 消息拦截器，实现ChannelInterceptor接口
         */
        registration.interceptors(webSocketChannelInterceptor);
    }
}

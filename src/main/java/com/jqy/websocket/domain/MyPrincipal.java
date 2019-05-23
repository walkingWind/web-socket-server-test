package com.jqy.websocket.domain;

import java.security.Principal;

/**
 * @Author: zhengc
 * @Date: 2019/5/22 17:52
 * @Version 1.0
 * @Note 自定义的Principal
 */
public class MyPrincipal implements Principal {
    private String key;

    public MyPrincipal(String key) {
        this.key = key;
    }

    @Override
    public String getName() {
        return key;
    }
}

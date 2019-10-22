package com.wss.demo.Controller;

import com.wss.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTest {

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("/get")
    public String test() {

        String name = (String)redisUtil.get("name");
        System.out.println(name);
        return name;

    }

    @RequestMapping("/set")
    public void testSet() {
        redisUtil.set("name","xqz");
    }

}

package com.wss.demo.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@RestController
public class TestController {

    private Jedis jedis = new Jedis("47.106.242.51",6379);

    @RequestMapping("/jget")
    public void testJGet() {

    }

    @RequestMapping("/jset")
    public void testJSet() {

    }

}

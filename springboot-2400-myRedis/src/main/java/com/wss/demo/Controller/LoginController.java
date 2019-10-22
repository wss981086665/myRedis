package com.wss.demo.Controller;

import com.wss.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Null;
import java.util.Date;

@RestController
public class LoginController {

    @Autowired
    RedisUtil redisUtil;

    private Integer LIMIT = 10000000;

    public String check_token(String token) {
        return (String)redisUtil.hget("login:", token);
    }

    public void update_token(String token, String user, @RequestParam(value = "item", defaultValue= "null") String item) {
        long timestamp = new Date().getTime() / 1000;
        redisUtil.hset("login:", token, user);
        redisUtil.zAdd("recent:", token, timestamp);

        if(!"null".equals(item)) {
            redisUtil.zAdd("viewed:" + token, item, timestamp);
            redisUtil.zRemrangebyrank("viewed:" + token, 0, -26);
        }
    }

}

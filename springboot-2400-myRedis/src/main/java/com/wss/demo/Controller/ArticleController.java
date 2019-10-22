package com.wss.demo.Controller;

import com.wss.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.*;

@RestController
public class ArticleController {

    @Autowired
    RedisUtil redisUtil;

    private Jedis jedis = new Jedis("47.106.242.51",6379);

    // 每一周的秒数
    private final Integer ONE_WEEK_IN_SECONDS = 7 * 86400;
    private final int VOTE_SCORE = 432;

    private final int ARTICLES_PER_PAGE = 25;

    @RequestMapping("/article_vote")
    public void article_vote(@RequestParam String user, @RequestParam String article) {

        // new Date().getTime()： 1970年1月1日至现在的秒数
        long cutoff = new Date().getTime() / 1000 - ONE_WEEK_IN_SECONDS;
        if(redisUtil.zScore("time:", article) < cutoff)
            return;

        // 获取文章的id，文章名称：'article:123456'
        String article_id = article.split(":")[1];
        if (redisUtil.sAdd("voted:" + article_id, user)) {

            // 在'score：'集合中为指定文章加上分数
            redisUtil.zIncrby("score:", article, VOTE_SCORE);

            // votes：已投票的人数，记录文章的投票数
            redisUtil.hincrby(article, "votes",1);
        }

    }

    @RequestMapping("/post_article")
    public long post_article(@RequestParam String user, @RequestParam String title, @RequestParam String link) {
        long article_id = redisUtil.incr("article:");

        String voted = "voted:" + article_id;
        redisUtil.sAdd(voted, user);
        redisUtil.expire(voted, ONE_WEEK_IN_SECONDS);

        long now = new Date().getTime() / 1000;
        String article = "article:" + article_id;
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("link", link);
        map.put("poster", user);
        map.put("time", now);
        map.put("votes", 1);
        redisUtil.hmset(article, map);

        redisUtil.zAdd("score:", article, now + VOTE_SCORE);
        redisUtil.zAdd("time:", article, now);

        return article_id;
    }

    @RequestMapping("/get_articles")
    public List<Map<String, String>> get_articles(@RequestParam int page, @RequestParam String order) {
        order = "score:";
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE - 1;

        Set<Object> set = redisUtil.zRevrange(order, start, end);
        List<Map<String, String>> list = new ArrayList<>();

        for (Object id : set) {
            Map<String, String> article_data = redisUtil.hgetall((String) id);
            article_data.replace((String) id, (String) id);
            list.add(article_data);
        }

        return list;
    }

    @RequestMapping("/add_remove_group")
    public void add_remove_group(String article_id, String[] to_add, String[] to_remove) {
        String article = "aericle_id" + article_id;
        for (String group : to_add)
            redisUtil.sAdd("group:" + group, article);
        for (String group : to_remove)
            redisUtil.setRemove("group:" + group, article);
    }

    @RequestMapping("/get_group_articles")
    public List<Map<String, String>> get_group_articles(String group, int page, String order) {
        order = "score:";
        String key = order + group;
        String[] key2 = {"group:" + group, order};
        String aggregate;
        if(!redisUtil.hasKey(key)) {
            redisUtil.zinterstore(key, key2, aggregate = "max");
            redisUtil.expire(key, 60);
        }
        return get_articles(page, key);
    }

}

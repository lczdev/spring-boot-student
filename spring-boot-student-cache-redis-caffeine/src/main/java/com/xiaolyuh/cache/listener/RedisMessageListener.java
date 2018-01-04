package com.xiaolyuh.cache.listener;

import com.alibaba.fastjson.JSON;
import com.xiaolyuh.cache.layering.LayeringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * redis消息的订阅者
 *
 * @author yuhao.wang
 */
@Component
public class RedisMessageListener extends MessageListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RedisPublisher.class);

    @Autowired
    CacheManager cacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        super.onMessage(message, pattern);
        logger.info("redis消息订阅者接收到频道【{}】发布的消息。消息内容：{}", new String(message.getChannel()), message.toString().getBytes());
        // 解析订阅发布的信息，获取缓存的名称和缓存的key
        String ms = new String(message.getBody());
        Map<String, Object> map = JSON.parseObject(ms, HashMap.class);
        String cacheName = (String) map.get("cacheName");
        Object key = map.get("key");

        // 根据缓存名称获取多级缓存
        Cache cache = cacheManager.getCache(cacheName);

        // 判断缓存是否是多级缓存
        if (cache != null && cache instanceof LayeringCache) {
            // 获取一级缓存，并删除一级缓存数据
            ((LayeringCache) cache).getFirstCache().evict(key);
            logger.info("删除一级缓存数据,key:{}", key.toString().getBytes());
        }
    }

}

package com.gccbenben.qqbotservice.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author GccBenben
 * @date 2021/08/05
 */
@Component
@Slf4j
public final class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * redis指定失效时间
     *
     * @param key  键
     * @param time 时间
     * @return boolean
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("redis失效操作失败" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取redis会话过期时间
     *
     * @param key 关键
     * @return long
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 是否存在指定key
     *
     * @param key 关键
     * @return boolean
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("redis获取key:{} 操作失败" + e.getMessage(), key);
            return false;
        }
    }

    /**
     * 删除
     *
     * @param key 关键
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 获取指定值
     *
     * @param key 关键
     * @return {@link Object}
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 存储
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public boolean put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("redis存储key:{}  value:{}   操作失败" + e.getMessage(), key, value.toString());
            return false;
        }
    }

    /**
     * 存储，同时设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间/秒
     * @return boolean
     */
    public boolean put(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                this.put(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("redis存储key:{}  value:{}   操作失败" + e.getMessage(), key, value.toString());
            return false;
        }
    }

    /**
     * 递增存放
     *
     * @param key   关键
     * @param delta δ
     * @return long
     */
    public long increase(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减存放
     *
     * @param key   关键
     * @param delta δ
     * @return long
     */
    public long decrease(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

}

package com.xc.luckysheet.redisserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * @author Administrator
 */
public class RedisLock {

	@Autowired
    private RedisTemplate redisTemplate;
    /**
     * 重试时间
     */
    private static final int DEFAULT_ACQUIRY_RETRY_MILLIS = 100;
    /**
     * 锁的前缀
     */
    private static final String LOCK_SUFFIX = "redis_lock:";
    /**
     * 锁的key
     */
    private String lockKey;
    /**
     * 锁超时时间，防止线程在入锁以后，防止阻塞后面的线程无法获取锁
     */
    private int expireMsecs = 60 * 1000;
    /**
     * 线程获取锁的等待时间
     */
    private int timeoutMsecs = 10 * 1000;
    /**
     * 是否锁定标志
     */
    private volatile boolean locked = false;

    /**
     * 当前线程
     */
    private String uid="";

    /**
     * 构造器
     * @param redisTemplate
     * @param lockKey 锁的key
     */
    public RedisLock(RedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = LOCK_SUFFIX+lockKey;
    }

    /**
     * 构造器
     * @param redisTemplate
     * @param lockKey 锁的key
     * @param timeoutMsecs 获取锁的超时时间
     */
    public RedisLock(RedisTemplate redisTemplate, String lockKey, int timeoutMsecs) {
        this(redisTemplate, lockKey);
        this.timeoutMsecs = timeoutMsecs;
    }
    public RedisLock(RedisTemplate redisTemplate, String lockKey, int timeoutMsecs, String uid) {
        this(redisTemplate, lockKey);
        this.timeoutMsecs = timeoutMsecs;
        this.uid=uid;
    }

    /**
     * 构造器
     * @param redisTemplate
     * @param lockKey 锁的key
     * @param timeoutMsecs 获取锁的超时时间
     * @param expireMsecs 锁的有效期
     */
    public RedisLock(RedisTemplate redisTemplate, String lockKey, int timeoutMsecs, int expireMsecs) {
        this(redisTemplate, lockKey, timeoutMsecs);
        this.expireMsecs = expireMsecs;
    }

    public String getLockKey() {
        return lockKey;
    }

    /**
     * 封装和jedis方法
     * @param key
     * @return
     */
    private String get(final String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 封装和jedis方法
     * @param key
     * @param value
     * @return
     */
    private boolean setNX(final String key, final String value) {
        return redisTemplate.opsForValue().setIfAbsent(key,value);
    }

    /**
     * 封装和jedis方法
     * @param key
     * @param value
     * @return
     */
    private String getSet(final String key, final String value) {
        Object obj = redisTemplate.opsForValue().getAndSet(key,value);
        return obj != null ? (String) obj : null;
    }

    /**
     * 获取锁
     * @return 获取锁成功返回ture，超时返回false
     * @throws InterruptedException
     */
    public synchronized boolean lock() throws InterruptedException {
        //线程获取锁的等待时间
        int timeout = timeoutMsecs;
        while (timeout >= 0) {
            //超时时间
            long expires = System.currentTimeMillis() + expireMsecs + 1;
            //锁到期时间
            String expiresStr = String.valueOf(expires);
            if (this.setNX(lockKey, expiresStr)) {
                locked = true;
                return true;
            }
            //redis里key的时间
            String currentValue = this.get(lockKey);
            //判断锁是否已经过期，过期则重新设置并获取
            if (currentValue != null && Long.parseLong(currentValue) < System.currentTimeMillis()) {
                //设置锁并返回旧值
                String oldValue = this.getSet(lockKey, expiresStr);
                //比较锁的时间，如果不一致则可能是其他锁已经修改了值并获取
                if (oldValue != null && oldValue.equals(currentValue)) {
                    locked = true;
                    return true;
                }
            }
            timeout -= DEFAULT_ACQUIRY_RETRY_MILLIS;
            //延时
            Thread.sleep(DEFAULT_ACQUIRY_RETRY_MILLIS);
        }
        return false;
    }
    /**
     * 释放获取到的锁
     */
    public  synchronized void unlock() {
        if (locked) {
            redisTemplate.delete(lockKey);
            locked = false;
        }
    }

}

/*


    //使用方法，创建RedisLock对象
    RedisLock lock = new RedisLock(redisTemplate, "lock_" + product.getId());
    try {
        if (lock.lock()) {}
    } finally {
        lock.unlock();
    }



*/



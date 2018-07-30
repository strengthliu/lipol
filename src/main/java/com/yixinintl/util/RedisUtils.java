package com.yixinintl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

//import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * RedisUtils 提供了一个template方法，负责对Jedis连接的获取与归还。
 * JedisAction<T> 和 JedisActionNoResult两种回调接口，适用于有无返回值两种情况。
 * 同时提供一些最常用函数的封装, 如get/set/zadd等。
 */
@Component("redisUtils")
public class RedisUtils {
    private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private JedisPool jedisPool;
//    private static Pool<Jedis> jedisPool;

    public static final int MAX_IDLE = 32;

    public static final int MAX_TOTAL = 32;

//    public Jedis getJedis(){
//        if(jedisPool != null){
//            return jedisPool.getResource();
//        }else{
//            return null;
//        }
//    }
//    static {
//        try {
//            final Properties properties = PropertiesLoaderUtils.loadAllProperties("redis.properties");
//            String host = properties.getProperty("redis.host");
//            Integer port = Integer.parseInt(properties.getProperty("redis.port")) ;
////            String password = configuration.getString("redis.master.password");
//            JedisPoolConfig poolConfig = new JedisPoolConfig();
//            poolConfig.setMaxIdle(MAX_IDLE);
//            poolConfig.setMaxTotal(MAX_TOTAL);
//            poolConfig.setTimeBetweenEvictionRunsMillis(-1);
//            jedisPool = new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT);
//
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        }
//
//    }

    /**
     * 执行有返回结果的action。
     */
//    public static <T> T execute(JedisAction<T> jedisAction) throws JedisException {
//        Jedis jedis = null;
//        boolean broken = false;
//        try {
//            jedis = jedisPool.getResource();
//            return jedisAction.action(jedis);
//        } catch (JedisConnectionException e) {
//            logger.error("Redis connection lost.", e);
//            broken = true;
//            throw e;
//        } finally {
//            closeResource(jedis, broken);
//        }
//    }

    /**
     * 执行无返回结果的action。
     */
//    public static void execute(JedisActionNoResult jedisAction) throws JedisException {
//        Jedis jedis = null;
//        boolean broken = false;
//        try {
//            jedis = jedisPool.getResource();
//            jedisAction.action(jedis);
//        } catch (JedisConnectionException e) {
//            logger.error("Redis connection lost.", e);
//            broken = true;
//            throw e;
//        } finally {
//            closeResource(jedis, broken);
//        }
//    }

    /**
     * 根据连接是否已中断的标志，分别调用returnBrokenResource或returnResource。
     */
    protected void closeResource(Jedis jedis, boolean connectionBroken) {
        if (jedis != null) {
            try {
                if (connectionBroken) {
                    jedisPool.returnBrokenResource(jedis);
                } else {
                    jedisPool.returnResource(jedis);
                }
            } catch (Exception e) {
                logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                closeJedis(jedis);
            }
        }
    }


    /**
     * 有返回结果的回调接口定义。
     */
//    public static interface JedisAction<T> {
//        T action(Jedis jedis);
//    }

    /**
     * 无返回结果的回调接口定义。
     */
//    public static interface JedisActionNoResult {
//        void action(Jedis jedis);
//    }

    // ////////////// 常用方法的封装 ///////////////////////// //

    // ////////////// 公共 ///////////////////////////

    /**
     * 删除key, 如果key存在返回true, 否则返回false。
     */
    public Boolean del(final String... keys) {
//        return execute(new JedisAction<Boolean>(Jedis jedis) {
//             Boolean action(Jedis jedis){
//                 return jedis.del(keys) == 1;
//             }
//        });
    	Jedis jedis = null;
    	boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(keys) >= 1;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public void flushDB() {
//        execute((Jedis jedis) -> jedis.flushDB());
        jedisPool.getResource().flushDB();
    }

    // ////////////// 关于String ///////////////////////////

    public Set<String> keys(final String pattern){
    	Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(pattern);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 如果key不存在, 返回null.
     */
    public String get(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }


    }

    /**
     * 如果key不存在, 返回null.
     */
    public Long getAsLong(final String key) {
        String result = get(key);
        return result != null ? Long.valueOf(result) : null;
    }

    /**
     * 如果key不存在, 返回null.
     */
    public Integer getAsInt(final String key) {
        String result = get(key);
        return result != null ? Integer.valueOf(result) : null;
    }

    public void set(final String key, final String value) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
//            return ((JedisAction<String>) j -> j.get(key)).action(jedis);
            jedis.set(key, value);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public void setex(final String key, final String value, final int seconds) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key, seconds, value);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public void append(String key, String value){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.append(key, value);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 如果key还不存在则进行设置，返回true，否则返回false.
     */
    public Boolean setnx(final String key, final String value) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.setnx(key, value) == 1;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public Long incr(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public Long incrBy(final String key, int score) {
        boolean broken = false;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incrBy(key, score);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public Long decr(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.decr(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    // ////////////// 关于List ///////////////////////////
    public void lpush(final String key, final String... values) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.lpush(key, values);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public String rpop(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.rpop(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 返回List长度, key不存在时返回0，key类型不是list时抛出异常.
     */
    public Long llen(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.llen(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 删除List中的第一个等于value的元素，value不存在或key不存在时返回false.
     */
    public Boolean lremOne(final String key, final String value) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrem(key, 1, value) == 1;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 删除List中的所有等于value的元素，value不存在或key不存在时返回false.
     */
    public Boolean lremAll(final String key, final String value) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrem(key, 0, value) > 0;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    // ////////////// 关于Sorted Set ///////////////////////////

    /**
     * 加入Sorted set, 如果member在Set里已存在, 只更新score并返回false, 否则返回true.
     */
    public Boolean zadd(final String key, final String member, final double score) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zadd(key, score, member) == 1;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 删除sorted set中的元素，成功删除返回true，key或member不存在返回false。
     */
    public Boolean zrem(final String key, final String member) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrem(key, member) == 1;
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }





    private static void closeJedis(Jedis jedis) {
        if ((jedis != null) && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                jedis.disconnect();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // ////////////// 关于hash ///////////////////////////
    public boolean hexists(String key, String field){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hexists(key, field);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public void hdel(String key, String... field){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.hdel(key, field);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public String hget(String key, String field){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hget(key, field);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public Map<String, String> hgetAll(String key){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hgetAll(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public long hset(String key, String field, String value){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hset(key, field, value);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    // ////////////// 关于sortedSet ///////////////////////////

    public Double zincrby(String key, String member, double score){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zincrby(key, score, member);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 返回sorted set长度, key不存在时返回0.
     */
    public Long zcard(final String key) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcard(key);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 返回key指定分数区间的成员个数
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zcount(String key, Double min, Double max){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcount(key, min, max);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    public Set<String> zrange(String key, Long start, Long stop){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrange(key, start, stop);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 当key不存在时返回null.
     */
    public Double zscore(final String key, final String member) {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zscore(key, member);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }

    /**
     * 递减列表
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Set<String> zrevrange(String key, Long start, Long stop){
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrange(key, start, stop);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
    }


	public void expire(String key, int EXPIRE) {
		// TODO Auto-generated method stub
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.expire( key, EXPIRE);
//            return jedis.zrevrange(key, start, stop);
        } catch (JedisConnectionException e) {
            logger.error("Redis connection lost.", e);
            broken = true;
            throw e;
        } finally {
            if (jedis != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(jedis);
                    } else {
                        jedisPool.returnResource(jedis);
                    }
                } catch (Exception e) {
                    logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                    closeJedis(jedis);
                }
            }
        }
		
	}
}

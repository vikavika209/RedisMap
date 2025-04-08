package org.redis;

import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

public class RedisBackedMap implements Map<String, String> {
    private final Jedis jedis;
    private final String redisName;

    public RedisBackedMap(Jedis jedis, String redisName) {
        this.jedis = jedis;
        this.redisName = redisName;
    }

    private String redisKey (String key) {
        return redisName + ":" + key;
    }

    @Override
    public int size() {
        return (int) jedis.keys(redisName + ":*").size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return jedis.exists(redisKey((String)key));
    }

    @Override
    public boolean containsValue(Object value) {
        for (String key : jedis.keys(redisName + ":*")) {
            if (Objects.equals(jedis.get(key), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(Object key) {
        return jedis.get(redisKey((String)key));
    }

    @Override
    public String put(String key, String value) {
        String redisKey = redisKey(key);
        String oldValue = jedis.get(redisKey);
        jedis.set(redisKey, value);
        return oldValue;
    }

    @Override
    public String remove(Object key) {
        String redisKey = redisKey((String)key);
        String oldValue = jedis.get(redisKey);
        jedis.del(redisKey);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        Set<String> redisKeys = jedis.keys(redisName + ":*");
        for (String redisKey : redisKeys) {
            jedis.del(redisKey);
        }
    }

    @Override
    public Collection<String> values() {
        return jedis.keys(redisName + ":*").stream()
                .map(redisKey -> jedis.get(redisKey))
                .toList();
    }

    @Override
    public Set<String> keySet() {
        return jedis.keys(redisName + ":*").stream()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return jedis.keys(redisName + ":*").stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        fullKey -> fullKey.replaceFirst(redisName + ":", ""),
                        fullKey -> jedis.get(fullKey)
                ))
                .entrySet();
    }
}

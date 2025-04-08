package org.redis;

import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisBackedMapTest {

    private Jedis jedis;
    private RedisBackedMap redisMap;

    @BeforeAll
    void setup() {
        jedis = new Jedis("localhost", 6379);
    }

    @BeforeEach
    void beforeEach() {
        redisMap = new RedisBackedMap(jedis, "testmap");
        redisMap.clear();
    }

    @AfterAll
    void cleanup() {
        jedis.close();
    }

    @Test
    void testPutAndGet() {
        redisMap.put("key1", "value1");
        assertEquals("value1", redisMap.get("key1"));
    }

    @Test
    void testOverwriteValue() {
        redisMap.put("key1", "value1");
        String old = redisMap.put("key1", "value2");
        assertEquals("value1", old);
        assertEquals("value2", redisMap.get("key1"));
    }

    @Test
    void testRemove() {
        redisMap.put("key1", "value1");
        String removed = redisMap.remove("key1");
        assertEquals("value1", removed);
        assertNull(redisMap.get("key1"));
    }

    @Test
    void testContainsKey() {
        redisMap.put("key1", "value1");
        assertTrue(redisMap.containsKey("key1"));
        assertFalse(redisMap.containsKey("key2"));
    }

    @Test
    void testContainsValue() {
        redisMap.put("key1", "value1");
        assertTrue(redisMap.containsValue("value1"));
        assertFalse(redisMap.containsValue("value2"));
    }

    @Test
    void testClear() {
        redisMap.put("a", "1");
        redisMap.put("b", "2");
        redisMap.clear();
        assertTrue(redisMap.isEmpty());
    }

    @Test
    void testSize() {
        redisMap.put("a", "1");
        redisMap.put("b", "2");
        assertEquals(2, redisMap.size());
    }

    @Test
    void testPutAll() {
        Map<String, String> m = new HashMap<>();
        m.put("a", "1");
        m.put("b", "2");
        redisMap.putAll(m);
        assertEquals(2, redisMap.size());
        assertEquals("2", redisMap.get("b"));
    }

    @Test
    void testKeySet() {
        redisMap.put("a", "1");
        redisMap.put("b", "2");
        Set<String> keys = redisMap.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("testmap:a"));
        assertTrue(keys.contains("testmap:b"));
    }

    @Test
    void testValues() {
        redisMap.put("a", "1");
        redisMap.put("b", "2");
        Collection<String> values = redisMap.values();
        assertTrue(values.containsAll(Arrays.asList("1", "2")));
    }

    @Test
    void testEntrySet() {
        redisMap.put("a", "1");
        redisMap.put("b", "2");
        Set<Map.Entry<String, String>> entries = redisMap.entrySet();
        assertEquals(2, entries.size());
    }

    @Test
    void testGetWithNullKey() {
        assertEquals(null, redisMap.get(null));
    }

    @Test
    void testPutNullKey() {
        assertEquals(null, redisMap.put(null, "value"));
    }

    @Test
    void testPutNullValueNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> redisMap.put("null-value-key", null));
    }

    @Test
    void testRemoveNonexistentKeyReturnsNull() {
        assertNull(redisMap.remove("nonexistent"));
    }

    @Test
    void testContainsKeyWithWrongTypeThrowsException() {
        assertThrows(ClassCastException.class, () -> redisMap.containsKey(123));
    }

    @Test
    void testContainsValueWithNull() {
        assertFalse(redisMap.containsValue(null));
    }
}

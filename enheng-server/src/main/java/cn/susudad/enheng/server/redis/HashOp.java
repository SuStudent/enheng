package cn.susudad.enheng.server.redis;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

public class HashOp extends Operation {

  private final HashOperations<String, String, String> HASH;

  public HashOp(StringRedisTemplate redisTemplate) {
    this.HASH = redisTemplate.opsForHash();
  }

  public Long hDel(String key, Collection<String> fields) {
    return HASH.delete(key, fields);
  }

  public Long hDel(String key, String... fields) {
    return HASH.delete(key, fields);
  }

  public boolean hExists(String key, String field) {
    return HASH.hasKey(key, field);
  }

  public <T> T hGet(String key, String field, Class<T> clz) {
    return deserialize(HASH.get(key, field), clz);
  }

  public <T> Map<String, T> hGetAll(String key, Class<T> clz) {
    Map<String, String> entries = HASH.entries(key);
    Map<String, T> map = new LinkedHashMap<>(entries.size());
    for (Entry<String, String> entry : entries.entrySet()) {
      map.put(entry.getKey(), deserialize(entry.getValue(), clz));
    }
    return map;
  }


  public long hIncrBy(String key, String field, long delta) {
    return HASH.increment(key, field, delta);
  }

  public double hIncrByFloat(String key, String field, double delta) {
    return HASH.increment(key, field, delta);
  }

  public Set<String> hKeys(String key) {
    return HASH.keys(key);
  }

  public Long hLen(String key) {
    return HASH.size(key);
  }

  public <T> List<T> hmGet(String key, Collection<String> fields, Class<T> clz) {
    List<String> list = HASH.multiGet(key, fields);
    return list.stream().map(str -> deserialize(str, clz)).collect(Collectors.toList());
  }

  public void hmSet(String key, Map<String, Object> entries) {
    Map<String, String> hashes = new LinkedHashMap<>(entries.size());
    for (Entry<String, Object> entry : entries.entrySet()) {
      hashes.put(entry.getKey(), serialize(entry.getValue()));
    }
    HASH.putAll(key, hashes);
  }

  public void hSet(String key, String field, Object value) {
    HASH.put(key, field, serialize(value));
  }

  public void hSetNX(String key, String field, Object value) {
    HASH.putIfAbsent(key, field, serialize(value));
  }

  public <T> List<T> hVals(String key, Class<T> clz) {
    List<String> values = HASH.values(key);
    return values.stream().map(str -> deserialize(str, clz)).collect(Collectors.toList());
  }

  public <T> Cursor<Entry<String, T>> scan(String key, ScanOptions scanOptions, Class<T> clz) {
    return new ConvertingCursor<>(HASH.scan(key, scanOptions),
        (source) -> Converters.entryOf(source.getKey(), this.deserialize(source.getValue(), clz)));
  }

  public String hRandField(String key) {
    return HASH.randomKey(key);
  }

  public List<String> hRandField(String key, long count) {
    return HASH.randomKeys(key, count);
  }

  public <T> Entry<String, T> hRandFieldWithValues(String key, Class<T> clz) {
    Entry<String, String> entry = HASH.randomEntry(key);
    if (entry == null) {
      return null;
    }
    return Converters.entryOf(entry.getKey(), deserialize(entry.getValue(), clz));
  }

  public <T> Map<String, T> hRandFieldWithValues(String key, long count, Class<T> clz) {
    Map<String, String> entries = HASH.randomEntries(key, count);
    if (entries == null) {
      return null;
    }
    Map<String, T> result = new LinkedHashMap<>(entries.size());
    entries.forEach((k, v) -> {
      result.put(k, deserialize(v, clz));
    });
    return result;
  }

  public Long hStrLen(String key, String hashKey) {
    return HASH.lengthOfValue(key, hashKey);
  }
}
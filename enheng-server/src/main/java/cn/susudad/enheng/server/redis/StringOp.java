package cn.susudad.enheng.server.redis;

import cn.susudad.enheng.common.utils.ReflectUtils;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisStringCommands.BitOperation;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description StringOp
 * @createTime 2022/8/15
 */
public class StringOp extends Operation {

  private final ValueOperations<String, String> STRING;

  private final StringRedisTemplate redisTemplate;

  public StringOp(StringRedisTemplate redisTemplate) {
    this.STRING = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  private byte[] rawKey(String key) {
    byte[] rawKey = null;
    try {
      rawKey = (byte[]) ReflectUtils.invoke(STRING, "rawKey", key);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
      rawKey = key.getBytes(StandardCharsets.UTF_8);
    }
    return rawKey;
  }

  public Integer append(String key, String value) {
    return STRING.append(key, value);
  }

  public Long bitCount(String key, int start, int end) {
    return redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(rawKey(key), start, end));
  }

  public List<Long> bitField(String key, final BitFieldSubCommands subCommands) {
    return STRING.bitField(key, subCommands);
  }

  public Long bitOp(BitOperation op, String destination, String... keys) {
    byte[][] keyArr = new byte[0][0];
    if (ArrayUtils.isNotEmpty(keys)) {
      keyArr = Arrays.stream(keys).map(this::rawKey).toArray(byte[][]::new);
    }
    byte[][] finalKeyArr = keyArr;
    return redisTemplate.execute((RedisCallback<Long>) con -> con.bitOp(op, rawKey(destination), finalKeyArr));
  }

  public Long bitPos(String key, boolean bit, Range<Long> range) {
    return redisTemplate.execute((RedisCallback<Long>) con -> con.bitPos(rawKey(key), bit, range));
  }

  public Long decr(String key) {
    return STRING.decrement(key);
  }

  public Long decrBy(String key, long delta) {
    return STRING.decrement(key, delta);
  }

  public <T> T get(String key, Class<T> clz) {
    return deserialize(STRING.get(key), clz);
  }

  public Boolean getBit(String key, long offset) {
    return STRING.getBit(key, offset);
  }

  public String getRange(String key, int start, int end) {
    return STRING.get(key, start, end);
  }

  public <T> T getSet(String key, Object obj, Class<T> clz) {
    String oldValue = STRING.getAndSet(key, serialize(obj));
    return deserialize(oldValue, clz);
  }

  public Long incr(String key) {
    return STRING.increment(key);
  }

  public Long incrBy(String key, long delta) {
    return STRING.increment(key, delta);
  }

  public Double incrBy(String key, double delta) {
    return STRING.increment(key, delta);
  }

  public <T> List<T> mGet(Collection<String> keys, Class<T> clz) {
    List<String> list = STRING.multiGet(keys);
    if (list == null) {
      return new ArrayList<>();
    }
    return list.stream().map(str -> deserialize(str, clz)).collect(Collectors.toList());
  }

  public void mSet(Map<String, Object> map) {
    if (MapUtils.isEmpty(map)) {
      return;
    }
    Map<String, String> mValues = new LinkedHashMap<>(map.size());
    for (Entry<String, Object> entry : map.entrySet()) {
      mValues.put(entry.getKey(), serialize(entry.getValue()));
    }
    STRING.multiSet(mValues);
  }

  public Boolean mSetNX(Map<String, Object> map) {
    if (MapUtils.isEmpty(map)) {
      return true;
    }
    Map<String, String> mValues = new LinkedHashMap<>(map.size());
    for (Entry<String, Object> entry : map.entrySet()) {
      mValues.put(entry.getKey(), serialize(entry.getValue()));
    }
    return STRING.multiSetIfAbsent(mValues);
  }

  public void set(String key, Object value) {
    STRING.set(key, serialize(value));
  }

  public void setEX(String key, Object value, long timeout, TimeUnit unit) {
    STRING.set(key, serialize(value), timeout, unit);
  }

  public Boolean set(String key, Object value, Expiration expiration, SetOption option) {
    return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.set(rawKey(key), rawKey(serialize(value)), expiration, option));
  }

  public Boolean setPX(String key, Object value) {
    return set(key, value, Expiration.persistent(), SetOption.ifPresent());
  }

  public Boolean setBit(String key, long offset, boolean value) {
    return STRING.setBit(key, offset, value);
  }

  public Boolean setNX(String key, Object value) {
    return STRING.setIfAbsent(key, serialize(value));
  }

  public void setRange(String key, Object value, long offset) {
    STRING.set(key, serialize(value), offset);
  }

  public Long strLen(String key) {
    return STRING.size(key);
  }

  public <T> T getDel(String key, Class<T> clz) {
    return deserialize(STRING.getAndDelete(key), clz);
  }

  public <T> T getEX(String key, long timeout, TimeUnit unit, Class<T> clz) {
    return deserialize(STRING.getAndExpire(key, timeout, unit), clz);
  }
}

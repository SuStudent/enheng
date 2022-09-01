package cn.susudad.enheng.server.redis;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ListOp
 * @createTime 2022/8/25
 */
public class ListOp extends Operation {

  private ListOperations<String, String> list;

  public ListOp(StringRedisTemplate redisTemplate) {
    this.list = redisTemplate.opsForList();
  }

  public <T> T bLPop(String key, long timeout, TimeUnit unit, Class<T> clz) {
    return deserialize(list.leftPop(key, timeout, unit), clz);
  }

  public <T> T bRPop(String key, long timeout, TimeUnit unit, Class<T> clz) {
    return deserialize(list.rightPop(key, timeout, unit), clz);
  }

  public <T> T bRPopLPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit, Class<T> clz) {
    return deserialize(list.rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit), clz);
  }

  public <T> T lIndex(String key, long index, Class<T> clz) {
    return deserialize(list.index(key, index), clz);
  }

  public Long lInsert(String key, Position position, Object pivot, Object value) {
    if (position == Position.BEFORE) {
      return list.leftPush(key, serialize(pivot), serialize(value));
    }
    return list.rightPush(key, serialize(pivot), serialize(value));
  }

  public Long lLen(String key) {
    return list.size(key);
  }

  public <T> T lPop(String key, Class<T> clz) {
    return deserialize(list.leftPop(key), clz);
  }

  public Long lPos(String key, Object element) {
    return list.indexOf(key, serialize(element));
  }

  public Long lPush(String key, Object... values) {
    return list.leftPushAll(key, Arrays.stream(values).map(this::serialize).collect(Collectors.toList()));
  }

  public Long lPush(String key, Collection<Object> values) {
    return list.leftPushAll(key, values.stream().map(this::serialize).collect(Collectors.toList()));
  }

  public Long lPush(String key, Object value) {
    return list.leftPush(key, serialize(value));
  }

  public Long lPushX(String key, Object value) {
    return list.leftPushIfPresent(key, serialize(value));
  }

  public <T> List<T> lRange(String key, long start, long end, Class<T> clz) {
    List<String> range = list.range(key, start, end);
    if (range == null) {
      return null;
    }
    return range.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toList());
  }

  public Long lRem(String key, long count, Object value) {
    return list.remove(key, count, serialize(value));
  }

  public void lSet(String key, long index, Object value) {
    list.set(key, index, serialize(value));
  }

  public void lTrim(String key, long start, long end) {
    list.trim(key, start, end);
  }

  public <T> T rPop(String key, Class<T> clz) {
    return deserialize(list.rightPop(key), clz);
  }

  public <T> T rPopLPush(String sourceKey, String destinationKey, Class<T> clz) {
    return deserialize(list.rightPopAndLeftPush(sourceKey, destinationKey), clz);
  }

  public Long rPush(String key, Object value) {
    return list.rightPush(key, serialize(value));
  }

  public Long rPushX(String key, Object value) {
    return list.rightPushIfPresent(key, serialize(value));
  }

}

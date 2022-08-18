package cn.susudad.enheng.server.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description StringOp
 * @createTime 2022/8/15
 */
public class StringOp extends Operation {

  private final ValueOperations<String, String> STRING;

  public StringOp(StringRedisTemplate redisTemplate) {
    this.STRING = redisTemplate.opsForValue();
  }

  public void set(String key, Object value) {
    STRING.set(key, serialize(value));
  }

  public <T> T get(String key, Class<T> clz) {
    return deserialize(STRING.get(key), clz);
  }

  public String getRange(String key, int start, int end) {
    return STRING.get(key, start, end);
  }

  public <T> T getSet(String key, Object obj, Class<T> clz) {
    String oldValue = STRING.getAndSet(key, serialize(obj));
    return deserialize(oldValue, clz);
  }

  public Boolean getBit(String key, long offset) {
    return STRING.getBit(key, offset);
  }

  public <T> List<T> mGet(Collection<String> keys, Class<T> clz) {
    List<String> list = STRING.multiGet(keys);
    if (list == null) {
      return new ArrayList<>();
    }
    return list.stream().map(str -> deserialize(str, clz)).collect(Collectors.toList());
  }
}
